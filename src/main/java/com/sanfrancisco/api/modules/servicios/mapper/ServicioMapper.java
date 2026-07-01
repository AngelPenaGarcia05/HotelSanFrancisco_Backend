package com.sanfrancisco.api.modules.servicios.mapper;

import com.sanfrancisco.api.modules.recepcion.entity.Estancia;
import com.sanfrancisco.api.modules.servicios.dto.request.CreateServicioRequest;
import com.sanfrancisco.api.modules.servicios.dto.response.ServicioResponse;
import com.sanfrancisco.api.modules.servicios.entity.Servicio;
import com.sanfrancisco.api.modules.servicios.entity.TipoServicio;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Mapper manual. El subtotal = cantidad * precioAplicado. Si no se envía precioAplicado
 * se toma el costoBase del tipo de servicio. La fechaConsumo por defecto es ahora.
 */
@Component
public class ServicioMapper {

    public Servicio toEntity(CreateServicioRequest request, TipoServicio tipoServicio, Estancia estancia) {
        BigDecimal precioAplicado = Optional.ofNullable(request.precioAplicado()).orElse(tipoServicio.getCostoBase());
        BigDecimal subtotal = BigDecimal.valueOf(request.cantidad()).multiply(precioAplicado);
        return Servicio.builder()
                .tipoServicio(tipoServicio)
                .estancia(estancia)
                .cantidad(request.cantidad())
                .precioAplicado(precioAplicado)
                .subtotal(subtotal)
                .observaciones(request.observaciones())
                .fechaConsumo(Optional.ofNullable(request.fechaConsumo()).orElse(LocalDateTime.now()))
                .build();
    }

    public ServicioResponse toResponse(Servicio entity) {
        TipoServicio t = entity.getTipoServicio();
        Estancia e = entity.getEstancia();
        return new ServicioResponse(
                entity.getServicioId(),
                t != null ? t.getTipoServicioId() : null,
                t != null ? t.getNombre() : null,
                e != null ? e.getEstanciaId() : null,
                entity.getCantidad(),
                entity.getPrecioAplicado(),
                entity.getSubtotal(),
                entity.getObservaciones(),
                entity.getFechaConsumo(),
                entity.getFechaCreacion(),
                entity.getFechaModificacion()
        );
    }
}
