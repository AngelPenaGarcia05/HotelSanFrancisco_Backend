package com.sanfrancisco.api.modules.pagos.mapper;

import com.sanfrancisco.api.modules.pagos.dto.request.CreatePagoRequest;
import com.sanfrancisco.api.modules.pagos.dto.request.UpdatePagoRequest;
import com.sanfrancisco.api.modules.pagos.dto.response.PagoResponse;
import com.sanfrancisco.api.modules.pagos.entity.MetodoPago;
import com.sanfrancisco.api.modules.pagos.entity.Pago;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.ventas.entity.Venta;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Mapper manual. Si no se envía fecha en el create se usa el instante actual.
 * Los vínculos a venta/reserva se resuelven en el service y se inyectan aquí.
 */
@Component
public class PagoMapper {

    public Pago toEntity(CreatePagoRequest request, MetodoPago metodoPago, Venta venta, Reserva reserva) {
        return Pago.builder()
                .metodoPago(metodoPago)
                .tipoPago(request.tipoPago())
                .fecha(Optional.ofNullable(request.fecha()).orElse(LocalDateTime.now()))
                .monto(request.monto())
                .comprobante(request.comprobante())
                .venta(venta)
                .reserva(reserva)
                .build();
    }

    public void updateEntity(Pago target, UpdatePagoRequest request, MetodoPago metodoPago) {
        if (request.tipoPago() != null) target.setTipoPago(request.tipoPago());
        if (request.fecha() != null) target.setFecha(request.fecha());
        if (request.monto() != null) target.setMonto(request.monto());
        if (request.comprobante() != null) target.setComprobante(request.comprobante());
        if (metodoPago != null) target.setMetodoPago(metodoPago);
    }

    public PagoResponse toResponse(Pago entity) {
        MetodoPago m = entity.getMetodoPago();
        Venta v = entity.getVenta();
        Reserva r = entity.getReserva();
        return new PagoResponse(
                entity.getPagoId(),
                m != null ? m.getMetodoPagoId() : null,
                m != null ? m.getNombre() : null,
                entity.getTipoPago(),
                entity.getFecha(),
                entity.getMonto(),
                entity.getComprobante(),
                v != null ? v.getVentaId() : null,
                r != null ? r.getReservaId() : null,
                entity.getFechaCreacion(),
                entity.getFechaModificacion()
        );
    }
}
