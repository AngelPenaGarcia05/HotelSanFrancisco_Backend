package com.sanfrancisco.api.modules.pagos.service.impl;

import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.pagos.dto.request.CreatePagoRequest;
import com.sanfrancisco.api.modules.pagos.dto.request.PagoFilterRequest;
import com.sanfrancisco.api.modules.pagos.dto.request.UpdatePagoRequest;
import com.sanfrancisco.api.modules.pagos.dto.response.PagoResponse;
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
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.recepcion.repository.ReservaRepository;
import com.sanfrancisco.api.modules.ventas.entity.Venta;
import com.sanfrancisco.api.modules.ventas.repository.VentaRepository;
import com.sanfrancisco.api.shared.exception.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final VentaRepository ventaRepository;
    private final ReservaRepository reservaRepository;
    private final PagoMapper pagoMapper;
    private final PagoEventPublisher eventPublisher;
    private final NotificacionClienteService notificacionClienteService;

    public PagoServiceImpl(PagoRepository pagoRepository,
                           MetodoPagoRepository metodoPagoRepository,
                           VentaRepository ventaRepository,
                           ReservaRepository reservaRepository,
                           PagoMapper pagoMapper,
                           PagoEventPublisher eventPublisher,
                           NotificacionClienteService notificacionClienteService) {
        this.pagoRepository = pagoRepository;
        this.metodoPagoRepository = metodoPagoRepository;
        this.ventaRepository = ventaRepository;
        this.reservaRepository = reservaRepository;
        this.pagoMapper = pagoMapper;
        this.eventPublisher = eventPublisher;
        this.notificacionClienteService = notificacionClienteService;
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

        return pagoMapper.toResponse(saved);
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

    private Pago obtenerOFallar(Integer pagoId) {
        return pagoRepository.findById(pagoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado: " + pagoId));
    }
}
