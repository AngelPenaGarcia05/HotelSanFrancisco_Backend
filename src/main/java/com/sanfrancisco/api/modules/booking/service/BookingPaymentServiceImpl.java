package com.sanfrancisco.api.modules.booking.service;

import com.sanfrancisco.api.modules.booking.dto.BookingConfirmationResponse;
import com.sanfrancisco.api.modules.booking.dto.ConfirmarPagoRequest;
import com.sanfrancisco.api.modules.booking.dto.CrearSesionPagoResponse;
import com.sanfrancisco.api.modules.notificaciones.dto.request.SendPaymentConfirmationRequest;
import com.sanfrancisco.api.modules.notificaciones.service.interfaces.NotificationService;
import com.sanfrancisco.api.modules.pagos.entity.Pago;
import com.sanfrancisco.api.modules.pagos.enums.TipoPago;
import com.sanfrancisco.api.modules.pagos.websocket.PagoEventPublisher;
import com.sanfrancisco.api.modules.pasarela.client.NiubizClient;
import com.sanfrancisco.api.modules.pasarela.client.NiubizClientException;
import com.sanfrancisco.api.modules.pasarela.config.NiubizProperties;
import com.sanfrancisco.api.modules.pasarela.dto.NiubizAuthorizationResult;
import com.sanfrancisco.api.modules.pasarela.dto.NiubizSessionData;
import com.sanfrancisco.api.modules.pasarela.entity.TransaccionPasarela;
import com.sanfrancisco.api.modules.pasarela.enums.EstadoTransaccionPasarela;
import com.sanfrancisco.api.modules.pasarela.repository.TransaccionPasarelaRepository;
import com.sanfrancisco.api.modules.recepcion.entity.Huesped;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.recepcion.entity.ReservaHabitacion;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import com.sanfrancisco.api.modules.recepcion.enums.ModalidadPago;
import com.sanfrancisco.api.modules.recepcion.repository.DetalleHuespedRepository;
import com.sanfrancisco.api.modules.recepcion.repository.ReservaHabitacionRepository;
import com.sanfrancisco.api.modules.recepcion.repository.ReservaRepository;
import com.sanfrancisco.api.modules.recepcion.websocket.ReservaEventPublisher;
import com.sanfrancisco.api.shared.exception.ConflictException;
import com.sanfrancisco.api.shared.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

/**
 * Orquestador del pago online del booking público. NO es transaccional a
 * propósito: la llamada HTTP a Niubiz no debe ocurrir dentro de una
 * transacción de BD, y cada desenlace (aprobado/rechazado/error) se persiste
 * de forma atómica en {@link BookingPaymentPersistence}.
 */
@Service
public class BookingPaymentServiceImpl implements BookingPaymentService {

    private static final Logger log = LoggerFactory.getLogger(BookingPaymentServiceImpl.class);
    private static final SecureRandom RNG = new SecureRandom();

    private final NiubizProperties niubizProperties;
    private final NiubizClient niubizClient;
    private final TransaccionPasarelaRepository transaccionRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaHabitacionRepository reservaHabitacionRepository;
    private final DetalleHuespedRepository detalleHuespedRepository;
    private final BookingPaymentPersistence persistence;
    private final BookingConfirmationFactory confirmationFactory;
    private final ReservaEventPublisher reservaEventPublisher;
    private final PagoEventPublisher pagoEventPublisher;
    private final NotificationService notificationService;

    public BookingPaymentServiceImpl(NiubizProperties niubizProperties,
                                     NiubizClient niubizClient,
                                     TransaccionPasarelaRepository transaccionRepository,
                                     ReservaRepository reservaRepository,
                                     ReservaHabitacionRepository reservaHabitacionRepository,
                                     DetalleHuespedRepository detalleHuespedRepository,
                                     BookingPaymentPersistence persistence,
                                     BookingConfirmationFactory confirmationFactory,
                                     ReservaEventPublisher reservaEventPublisher,
                                     PagoEventPublisher pagoEventPublisher,
                                     NotificationService notificationService) {
        this.niubizProperties = niubizProperties;
        this.niubizClient = niubizClient;
        this.transaccionRepository = transaccionRepository;
        this.reservaRepository = reservaRepository;
        this.reservaHabitacionRepository = reservaHabitacionRepository;
        this.detalleHuespedRepository = detalleHuespedRepository;
        this.persistence = persistence;
        this.confirmationFactory = confirmationFactory;
        this.reservaEventPublisher = reservaEventPublisher;
        this.pagoEventPublisher = pagoEventPublisher;
        this.notificationService = notificationService;
    }

    @Override
    public CrearSesionPagoResponse crearSesionPago(Integer reservaId, String clientIp) {
        if (!niubizProperties.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El pago online no está disponible en este momento");
        }

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));

        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ConflictException("La reserva no admite pago online (estado " + reserva.getEstado() + ")");
        }

        BigDecimal monto = reserva.getAdelanto();
        if (monto == null || monto.signum() <= 0) {
            throw new ValidationException("La reserva no tiene un monto de pago online pendiente");
        }

        String correo = detalleHuespedRepository.findPrincipalConHuesped(reservaId)
                .map(dh -> dh.getHuesped() != null ? dh.getHuesped().getCorreo() : null)
                .orElse(null);

        NiubizSessionData session;
        try {
            session = niubizClient.crearSesion(monto, clientIp, correo);
        } catch (NiubizClientException e) {
            // Aquí aún no hay cobro en juego: fallar la sesión es seguro y reintentable.
            log.error("No se pudo crear la sesión Niubiz para la reserva {}: {}", reservaId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "No se pudo iniciar el pago con la pasarela. Intente nuevamente en unos minutos.");
        }

        TransaccionPasarela trx = TransaccionPasarela.builder()
                .reserva(reserva)
                .purchaseNumber(generarPurchaseNumber())
                .monto(monto)
                .moneda("PEN")
                .estado(EstadoTransaccionPasarela.CREADA)
                .sessionKey(session.sessionKey())
                .build();
        trx = transaccionRepository.save(trx);

        return new CrearSesionPagoResponse(
                reserva.getReservaId(),
                trx.getPurchaseNumber(),
                session.sessionKey(),
                niubizProperties.getMerchantId(),
                monto,
                "PEN",
                niubizProperties.getCheckoutScriptUrl(),
                session.expirationTime());
    }

    @Override
    public BookingConfirmationResponse confirmarPago(ConfirmarPagoRequest request) {
        TransaccionPasarela trx = transaccionRepository.findByPurchaseNumberFetchReserva(request.purchaseNumber())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transacción no encontrada"));

        Reserva reserva = trx.getReserva();

        // Idempotencia: si ya se autorizó (doble clic / reintento del navegador),
        // se devuelve la confirmación existente sin volver a cobrar.
        if (trx.getEstado() == EstadoTransaccionPasarela.AUTORIZADA) {
            return buildConfirmation(reserva);
        }
        if (trx.getEstado() != EstadoTransaccionPasarela.CREADA) {
            throw new ConflictException("La transacción ya fue procesada (estado " + trx.getEstado()
                    + "). Genere una nueva sesión de pago.");
        }
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ConflictException("La reserva ya no admite confirmación de pago (estado "
                    + reserva.getEstado() + ")");
        }

        NiubizAuthorizationResult result;
        try {
            result = niubizClient.autorizar(request.transactionToken(),
                    trx.getPurchaseNumber(), trx.getMonto(), trx.getMoneda());
        } catch (NiubizClientException e) {
            // Sin respuesta concluyente: el cobro pudo o no haberse realizado.
            // Se marca ERROR (no RECHAZADA) para reconciliar contra el panel Niubiz.
            log.error("Autorización Niubiz indeterminada para purchaseNumber {}: {}",
                    trx.getPurchaseNumber(), e.getMessage());
            persistence.registrarError(trx, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "No se pudo verificar el resultado del pago. No vuelva a intentar: "
                            + "el hotel confirmará su reserva por correo.");
        }

        if (!result.aprobado()) {
            persistence.registrarRechazo(trx, result);
            String detalle = result.descripcion() != null ? result.descripcion() : "operación denegada";
            throw new ValidationException("El pago fue rechazado: " + detalle
                    + ". Puede intentar nuevamente con otro medio de pago.");
        }

        Pago pago = persistence.registrarAprobacion(trx, result);

        // Eventos y correo fuera de la transacción: si fallan no deben deshacer el cobro.
        reservaEventPublisher.publishStateChanged(reserva);
        pagoEventPublisher.publishCreated(pago);
        try {
            notificationService.sendPaymentConfirmation(new SendPaymentConfirmationRequest(pago.getPagoId()));
        } catch (Exception e) {
            log.warn("No se pudo enviar el correo de confirmación del pago {}: {}",
                    pago.getPagoId(), e.getMessage());
        }

        return buildConfirmation(reserva);
    }

    @Override
    public BookingConfirmationResponse confirmacionPorPurchaseNumber(String purchaseNumber) {
        TransaccionPasarela trx = transaccionRepository.findByPurchaseNumberFetchReserva(purchaseNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transacción no encontrada"));
        if (trx.getEstado() != EstadoTransaccionPasarela.AUTORIZADA) {
            throw new ConflictException("La transacción no tiene un pago autorizado (estado "
                    + trx.getEstado() + ")");
        }
        return buildConfirmation(trx.getReserva());
    }

    private BookingConfirmationResponse buildConfirmation(Reserva reserva) {
        List<ReservaHabitacion> rhs =
                reservaHabitacionRepository.findByReservaIdFetchHabitacion(reserva.getReservaId());
        if (rhs.isEmpty()) {
            throw new IllegalStateException("Reserva sin habitación asignada: " + reserva.getReservaId());
        }
        Huesped huesped = detalleHuespedRepository.findPrincipalConHuesped(reserva.getReservaId())
                .map(dh -> dh.getHuesped())
                .orElseThrow(() -> new IllegalStateException(
                        "Reserva sin huésped principal: " + reserva.getReservaId()));

        TipoPago tipoPago = reserva.getModalidadPago() == ModalidadPago.PARCIAL
                ? TipoPago.ANTICIPO : TipoPago.TOTAL;
        return confirmationFactory.build(reserva, rhs, huesped, tipoPago);
    }

    /** Número de compra Niubiz: numérico, único, máx. 12 dígitos. */
    private String generarPurchaseNumber() {
        String numero;
        do {
            StringBuilder sb = new StringBuilder();
            sb.append(1 + RNG.nextInt(9));
            for (int i = 1; i < 12; i++) {
                sb.append(RNG.nextInt(10));
            }
            numero = sb.toString();
        } while (transaccionRepository.existsByPurchaseNumber(numero));
        return numero;
    }
}
