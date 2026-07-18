package com.sanfrancisco.api.modules.pagos.service.impl;

import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.pagos.dto.request.CreatePagoRequest;
import com.sanfrancisco.api.modules.pagos.dto.request.PagoFilterRequest;
import com.sanfrancisco.api.modules.pagos.dto.request.UpdatePagoRequest;
import com.sanfrancisco.api.modules.pagos.dto.response.PagoResponse;
import com.sanfrancisco.api.modules.pagos.dto.response.ResumenPagosReservaResponse;
import com.sanfrancisco.api.modules.pagos.enums.TipoPago;
import com.sanfrancisco.api.modules.pagos.entity.MetodoPago;
import com.sanfrancisco.api.modules.pagos.entity.Pago;
import com.sanfrancisco.api.modules.pagos.mapper.PagoMapper;
import com.sanfrancisco.api.modules.pagos.repository.MetodoPagoRepository;
import com.sanfrancisco.api.modules.pagos.repository.PagoRepository;
import com.sanfrancisco.api.modules.pagos.service.interfaces.PagoService;
import com.sanfrancisco.api.modules.pagos.specification.PagoSpecification;
import com.sanfrancisco.api.modules.pagos.websocket.PagoEventPublisher;
import com.sanfrancisco.api.modules.notificacionescliente.enums.TipoNotificacionHuesped;
import com.sanfrancisco.api.modules.notificacionescliente.service.interfaces.NotificacionClienteService;
import com.sanfrancisco.api.modules.notificaciones.dto.request.SendPaymentConfirmationRequest;
import com.sanfrancisco.api.modules.notificaciones.service.interfaces.NotificationService;
import com.sanfrancisco.api.modules.recepcion.dto.request.CambiarEstadoReservaRequest;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import com.sanfrancisco.api.modules.recepcion.enums.ModalidadPago;
import com.sanfrancisco.api.modules.recepcion.repository.ReservaRepository;
import com.sanfrancisco.api.modules.recepcion.service.interfaces.ReservaService;
import com.sanfrancisco.api.modules.ventas.entity.Venta;
import com.sanfrancisco.api.modules.ventas.repository.VentaRepository;
import com.sanfrancisco.api.shared.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class PagoServiceImpl implements PagoService {

    private static final Logger log = LoggerFactory.getLogger(PagoServiceImpl.class);

    private final PagoRepository pagoRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final VentaRepository ventaRepository;
    private final ReservaRepository reservaRepository;
    private final PagoMapper pagoMapper;
    private final PagoEventPublisher eventPublisher;
    private final NotificacionClienteService notificacionClienteService;
    private final NotificationService notificationService;
    private final ReservaService reservaService;

    public PagoServiceImpl(PagoRepository pagoRepository,
                           MetodoPagoRepository metodoPagoRepository,
                           VentaRepository ventaRepository,
                           ReservaRepository reservaRepository,
                           PagoMapper pagoMapper,
                           PagoEventPublisher eventPublisher,
                           NotificacionClienteService notificacionClienteService,
                           NotificationService notificationService,
                           ReservaService reservaService) {
        this.pagoRepository = pagoRepository;
        this.metodoPagoRepository = metodoPagoRepository;
        this.ventaRepository = ventaRepository;
        this.reservaRepository = reservaRepository;
        this.pagoMapper = pagoMapper;
        this.eventPublisher = eventPublisher;
        this.notificacionClienteService = notificacionClienteService;
        this.notificationService = notificationService;
        this.reservaService = reservaService;
    }

    @Override
    public PagoResponse create(CreatePagoRequest request) {
        if (request.ventaId() == null && request.reservaId() == null) {
            throw new ValidationException("El pago debe estar asociado a una venta o a una reserva");
        }

        MetodoPago metodoPago = metodoPagoRepository.findById(request.metodoPagoId())
                .orElseThrow(() -> new ResourceNotFoundException("MetodoPago no encontrado: " + request.metodoPagoId()));

        if (Boolean.TRUE.equals(metodoPago.getRequiereComprobante())
                && (request.comprobante() == null || request.comprobante().isBlank())) {
            throw new ValidationException("El método de pago " + metodoPago.getNombre() + " requiere comprobante");
        }

        Venta venta = null;
        if (request.ventaId() != null) {
            venta = ventaRepository.findById(request.ventaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada: " + request.ventaId()));
        }

        Reserva reserva = null;
        if (request.reservaId() != null) {
            reserva = reservaRepository.findById(request.reservaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada: " + request.reservaId()));
        }

        if (venta != null) {
            validarSaldo(request.monto(), venta.getMontoTotal(),
                    pagoRepository.findByVentaVentaId(venta.getVentaId()), "la venta");
        }
        if (reserva != null) {
            validarSaldo(request.monto(), reserva.getMontoTotal(),
                    pagoRepository.findByReservaReservaId(reserva.getReservaId()), "la reserva");
        }

        Pago saved = pagoRepository.save(pagoMapper.toEntity(request, metodoPago, venta, reserva));
        eventPublisher.publishCreated(saved);

        if (reserva != null && reserva.getUsuario() != null) {
            notificacionClienteService.registrar(
                    reserva.getUsuario().getUsuarioId(),
                    TipoNotificacionHuesped.PAGO,
                    "Pago registrado",
                    "Se registró un pago de S/ " + saved.getMonto()
                            + " para tu reserva " + reserva.getCodReserva() + ".",
                    reserva.getReservaId());
        }

        if (reserva != null) {
            try {
                notificationService.sendPaymentConfirmation(new SendPaymentConfirmationRequest(saved.getPagoId()));
            } catch (Exception e) {
                log.warn("No se pudo enviar el correo de confirmación del pago {}: {}", saved.getPagoId(), e.getMessage());
            }
        }

        return pagoMapper.toResponse(saved);
    }

    @Override
    public PagoResponse registrarPagoInicialEfectivo(Integer reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada: " + reservaId));

        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ValidationException("El pago inicial solo aplica a reservas PENDIENTES (estado actual: "
                    + reserva.getEstado() + ")");
        }
        BigDecimal monto = reserva.getAdelanto();
        if (monto == null || monto.signum() <= 0) {
            throw new ValidationException("La reserva no tiene un adelanto pendiente de cobro");
        }

        MetodoPago efectivo = metodoPagoRepository.findFirstByNombreIgnoreCase("Efectivo")
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el método de pago 'Efectivo' en el catálogo (migración V47)"));

        TipoPago tipoPago = reserva.getModalidadPago() == ModalidadPago.PARCIAL
                ? TipoPago.ANTICIPO : TipoPago.TOTAL;

        // Reutiliza create(): valida saldo, publica el evento, notifica y envía el correo.
        PagoResponse pago = create(new CreatePagoRequest(
                efectivo.getMetodoPagoId(), tipoPago, null, monto, null, null, reservaId));

        // Igual que la autorización Niubiz: el cobro inicial confirma la reserva
        // (transiciones, historial y notificaciones del flujo estándar).
        reservaService.cambiarEstado(reservaId,
                new CambiarEstadoReservaRequest(EstadoReserva.CONFIRMADA, "Pago inicial en efectivo"));

        log.info("Pago inicial en efectivo registrado: reserva {} ({}), monto S/ {}",
                reserva.getCodReserva(), tipoPago, monto);
        return pago;
    }

    @Override
    public PagoResponse update(Integer pagoId, UpdatePagoRequest request) {
        Pago pago = obtenerOFallar(pagoId);

        MetodoPago metodoPago = null;
        if (request.metodoPagoId() != null) {
            metodoPago = metodoPagoRepository.findById(request.metodoPagoId())
                    .orElseThrow(() -> new ResourceNotFoundException("MetodoPago no encontrado: " + request.metodoPagoId()));
        }

        pagoMapper.updateEntity(pago, request, metodoPago);

        MetodoPago metodoFinal = pago.getMetodoPago();
        if (metodoFinal != null && Boolean.TRUE.equals(metodoFinal.getRequiereComprobante())
                && (pago.getComprobante() == null || pago.getComprobante().isBlank())) {
            throw new ValidationException("El método de pago " + metodoFinal.getNombre() + " requiere comprobante");
        }

        Pago saved = pagoRepository.save(pago);
        eventPublisher.publishUpdated(saved);
        return pagoMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponse findById(Integer pagoId) {
        return pagoMapper.toResponse(obtenerOFallar(pagoId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PagoResponse> search(PagoFilterRequest filter, Pageable pageable) {
        return pagoRepository.findAll(PagoSpecification.build(filter), pageable)
                .map(pagoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> findByReserva(Integer reservaId) {
        return pagoRepository.findByReservaReservaId(reservaId).stream()
                .map(pagoMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenPagosReservaResponse resumenByReserva(Integer reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada: " + reservaId));

        // Los REEMBOLSO restan del total pagado; el resto (ANTICIPO/SALDO/TOTAL) suma.
        BigDecimal totalPagado = pagoRepository.findByReservaReservaId(reservaId).stream()
                .map(p -> p.getTipoPago() == TipoPago.REEMBOLSO ? p.getMonto().negate() : p.getMonto())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal montoTotal = reserva.getMontoTotal() != null ? reserva.getMontoTotal() : BigDecimal.ZERO;
        BigDecimal saldo = montoTotal.subtract(totalPagado).max(BigDecimal.ZERO);

        return new ResumenPagosReservaResponse(
                reservaId, montoTotal, reserva.getAdelanto(), totalPagado, saldo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> findByVenta(Integer ventaId) {
        return pagoRepository.findByVentaVentaId(ventaId).stream()
                .map(pagoMapper::toResponse).toList();
    }

    @Override
    public void deleteById(Integer pagoId) {
        Pago pago = obtenerOFallar(pagoId);
        pagoRepository.delete(pago);
        eventPublisher.publishDeleted(pago.getPagoId());
    }

    private void validarSaldo(BigDecimal nuevoMonto, BigDecimal montoTotal, List<Pago> pagosPrevios, String entidad) {
        if (montoTotal == null) {
            return;
        }
        BigDecimal pagado = pagosPrevios.stream()
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal saldo = montoTotal.subtract(pagado);
        if (nuevoMonto.compareTo(saldo) > 0) {
            throw new ValidationException("El pago de S/ " + nuevoMonto + " excede el saldo pendiente de "
                    + entidad + ": S/ " + saldo.max(BigDecimal.ZERO));
        }
    }

    private Pago obtenerOFallar(Integer pagoId) {
        return pagoRepository.findById(pagoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado: " + pagoId));
    }
}
