package com.sanfrancisco.api.modules.booking.service;

import com.sanfrancisco.api.modules.booking.dto.BookingConfirmationResponse;
import com.sanfrancisco.api.modules.booking.dto.CreateBookingRequest;
import com.sanfrancisco.api.modules.booking.dto.HabitacionDisponibleResponse;
import com.sanfrancisco.api.modules.booking.dto.MetodoPagoPublicoResponse;
import com.sanfrancisco.api.modules.pagos.enums.TipoPago;
import com.sanfrancisco.api.modules.pagos.repository.MetodoPagoRepository;
import com.sanfrancisco.api.modules.recepcion.entity.*;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReservaHabitacion;
import com.sanfrancisco.api.modules.recepcion.enums.ModalidadPago;
import com.sanfrancisco.api.modules.recepcion.repository.*;
import com.sanfrancisco.api.modules.recepcion.websocket.ReservaEventPublisher;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import com.sanfrancisco.api.shared.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class BookingServiceImpl implements BookingService {

    private static final String SYSTEM_USER_EMAIL = "sistema.web@hotel-sf.com";
    private static final String CANAL_WEB = "Web";
    private static final BigDecimal IGV = new BigDecimal("0.18");
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RNG = new SecureRandom();

    private final HabitacionRepository habitacionRepository;
    private final HuespedRepository huespedRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaHabitacionRepository reservaHabitacionRepository;
    private final DetalleHuespedRepository detalleHuespedRepository;
    private final CanalRepository canalRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaEventPublisher reservaEventPublisher;
    private final BookingConfirmationFactory confirmationFactory;

    public BookingServiceImpl(
            HabitacionRepository habitacionRepository,
            HuespedRepository huespedRepository,
            ReservaRepository reservaRepository,
            ReservaHabitacionRepository reservaHabitacionRepository,
            DetalleHuespedRepository detalleHuespedRepository,
            CanalRepository canalRepository,
            MetodoPagoRepository metodoPagoRepository,
            UsuarioRepository usuarioRepository,
            ReservaEventPublisher reservaEventPublisher,
            BookingConfirmationFactory confirmationFactory) {
        this.habitacionRepository = habitacionRepository;
        this.huespedRepository = huespedRepository;
        this.reservaRepository = reservaRepository;
        this.reservaHabitacionRepository = reservaHabitacionRepository;
        this.detalleHuespedRepository = detalleHuespedRepository;
        this.canalRepository = canalRepository;
        this.metodoPagoRepository = metodoPagoRepository;
        this.usuarioRepository = usuarioRepository;
        this.reservaEventPublisher = reservaEventPublisher;
        this.confirmationFactory = confirmationFactory;
    }

    @Override
    public List<HabitacionDisponibleResponse> findDisponibles(LocalDate fechaInicio, LocalDate fechaFin, Integer personas) {
        if (!fechaFin.isAfter(fechaInicio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de salida debe ser posterior a la fecha de entrada");
        }
        List<Habitacion> disponibles = habitacionRepository.findDisponiblesParaFechas(fechaInicio, fechaFin, personas);
        return disponibles.stream().map(h -> {
            TipoHabitacion t = h.getTipoHabitacion();
            return new HabitacionDisponibleResponse(
                    h.getHabitacionId(),
                    h.getNumero(),
                    h.getPiso(),
                    t.getTipoHabitacionId(),
                    t.getNombre(),
                    h.getDescripcion(),
                    t.getPrecioBase(),
                    t.getCapacidadMaxima()
            );
        }).toList();
    }

    @Override
    public List<MetodoPagoPublicoResponse> findMetodosPago() {
        return metodoPagoRepository.findByEstado(EstadoActivo.ACTIVO).stream()
                .map(m -> new MetodoPagoPublicoResponse(m.getMetodoPagoId(), m.getNombre(), m.getRequiereComprobante()))
                .toList();
    }

    /**
     * Crea la pre-reserva del flujo público. Este flujo solo admite pago online
     * (regla de negocio: el huésped web siempre deja adelanto o paga el total);
     * el pago presencial/efectivo es exclusivo de recepción. La reserva nace
     * PENDIENTE sin registrar ningún pago y se confirma únicamente cuando
     * Niubiz autoriza el cobro (BookingPaymentService). Si no se paga, el job
     * de expiración la cancela y libera la habitación.
     */
    @Override
    @Transactional
    public BookingConfirmationResponse crearReserva(CreateBookingRequest req) {
        if (!req.fechaFin().isAfter(req.fechaInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de salida debe ser posterior a la fecha de entrada");
        }

        // Lock pesimista: serializa reservas concurrentes sobre la misma habitación para
        // que la validación de solapamiento siguiente sea fiable hasta el commit.
        Habitacion habitacion = habitacionRepository.findAllByIdForUpdate(List.of(req.habitacionId())).stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Habitación no encontrada"));

        TipoHabitacion tipo = habitacion.getTipoHabitacion();
        if (tipo == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La habitación no tiene tipo asignado");
        }

        validarOcupacion(req.nroAdultos(), req.nroNinos(), tipo);

        boolean ocupada = reservaHabitacionRepository.existeSolapamiento(
                habitacion.getHabitacionId(), req.fechaInicio(), req.fechaFin(),
                Set.of(EstadoReserva.CANCELADA, EstadoReserva.NO_SHOW), null);
        if (ocupada) {
            throw new com.sanfrancisco.api.shared.exception.ConflictException(
                    "La habitación ya no está disponible para las fechas seleccionadas");
        }

        Usuario sistemaUser = getSistemaUser();
        Canal canal = canalRepository.findByEstado(EstadoActivo.ACTIVO).stream()
                .filter(c -> CANAL_WEB.equalsIgnoreCase(c.getNombre()))
                .findFirst()
                .orElse(null);

        // Huesped: buscar o crear por número de documento
        Huesped huesped = huespedRepository.findByNumeroDocumento(req.numeroDocumento())
                .orElseGet(() -> {
                    Huesped nuevo = Huesped.builder()
                            .numeroDocumento(req.numeroDocumento())
                            .nombre(req.nombres())
                            .apellidoPaterno(req.apellidos())
                            .correo(req.correo())
                            .telefono(req.telefono())
                            .estado(EstadoActivo.ACTIVO)
                            .build();
                    return huespedRepository.save(nuevo);
                });

        // Calcular montos
        long noches = req.fechaInicio().until(req.fechaFin()).getDays();
        if (noches <= 0) noches = 1;

        BigDecimal precioNoche = tipo.getPrecioBase();
        BigDecimal subtotal = precioNoche.multiply(BigDecimal.valueOf(noches)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal impuesto = subtotal.multiply(IGV).setScale(2, RoundingMode.HALF_UP);
        BigDecimal montoTotal = subtotal.add(impuesto).setScale(2, RoundingMode.HALF_UP);

        BigDecimal adelanto = req.tipoPago() == TipoPago.TOTAL
                ? montoTotal
                : montoTotal.multiply(new BigDecimal("0.50")).setScale(2, RoundingMode.HALF_UP);

        // Crear reserva
        String codReserva = generarCodReservaUnico();
        Reserva reserva = Reserva.builder()
                .codReserva(codReserva)
                .fechaInicio(req.fechaInicio())
                .fechaFin(req.fechaFin())
                .nroAdultos(req.nroAdultos())
                .nroNinos(req.nroNinos() != null ? req.nroNinos() : 0)
                .subtotal(subtotal)
                .descuento(BigDecimal.ZERO)
                .impuesto(impuesto)
                .montoTotal(montoTotal)
                .adelanto(adelanto)
                .modalidadPago(req.tipoPago() == TipoPago.ANTICIPO
                        ? ModalidadPago.PARCIAL : ModalidadPago.TOTAL)
                // PENDIENTE bloquea la habitación hasta que Niubiz autorice el cobro.
                .estado(EstadoReserva.PENDIENTE)
                .observaciones(req.serviciosAdicionales())
                .usuario(sistemaUser)
                .canal(canal)
                .build();
        reserva = reservaRepository.save(reserva);

        // ReservaHabitacion
        ReservaHabitacion rh = ReservaHabitacion.builder()
                .reserva(reserva)
                .habitacion(habitacion)
                .tipoHabitacion(tipo)
                .tarifaPactada(precioNoche)
                .noches((int) noches)
                .subtotal(subtotal)
                .estado(EstadoReservaHabitacion.RESERVADA)
                .build();
        reservaHabitacionRepository.save(rh);

        // DetalleHuesped
        DetalleHuespedPK pk = new DetalleHuespedPK(huesped.getHuespedId(), reserva.getReservaId());
        DetalleHuesped dh = DetalleHuesped.builder()
                .id(pk)
                .huesped(huesped)
                .reserva(reserva)
                .esPrincipal(true)
                .build();
        detalleHuespedRepository.save(dh);

        // Mismo evento/topic que el flujo de recepción: sin esto el panel admin
        // no se entera en tiempo real de las reservas creadas desde la web pública.
        reservaEventPublisher.publishCreated(reserva);

        return confirmationFactory.build(reserva, rh, huesped, req.tipoPago());
    }

    /**
     * Valida la ocupación contra la capacidad del tipo de habitación:
     * al menos 1 adulto y (adultos + niños) dentro de la capacidad máxima.
     */
    private void validarOcupacion(Integer nroAdultos, Integer nroNinos, TipoHabitacion tipo) {
        int adultos = nroAdultos != null ? nroAdultos : 0;
        int ninos = nroNinos != null ? nroNinos : 0;
        if (adultos < 1) {
            throw new ValidationException("La reserva debe incluir al menos un adulto");
        }
        Integer capacidad = tipo.getCapacidadMaxima();
        if (capacidad != null && adultos + ninos > capacidad) {
            throw new ValidationException("El número de huéspedes (" + (adultos + ninos)
                    + ") excede la capacidad máxima de la habitación tipo "
                    + tipo.getNombre() + " (" + capacidad + ")");
        }
    }

    private Usuario getSistemaUser() {
        return usuarioRepository.findByCorreo(SYSTEM_USER_EMAIL)
                .or(() -> usuarioRepository.findFirstByRolNombreOrderByUsuarioIdAsc("ADMIN"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "No se encontró usuario del sistema para procesar la reserva"));
    }

    private String generarCodReservaUnico() {
        String cod;
        do {
            StringBuilder sb = new StringBuilder("SK-");
            for (int i = 0; i < 6; i++) {
                sb.append(CHARS.charAt(RNG.nextInt(CHARS.length())));
            }
            cod = sb.toString();
        } while (reservaRepository.existsByCodReserva(cod));
        return cod;
    }
}
