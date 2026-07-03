package com.sanfrancisco.api.modules.servicios.mapper;

import com.sanfrancisco.api.modules.servicios.dto.request.CreateTipoServicioRequest;
import com.sanfrancisco.api.modules.servicios.dto.request.UpdateTipoServicioRequest;
import com.sanfrancisco.api.modules.servicios.dto.response.TipoServicioResponse;
import com.sanfrancisco.api.modules.servicios.entity.TipoServicio;
import org.springframework.stereotype.Component;

@Component
public class TipoServicioMapper {

    public TipoServicio toEntity(CreateTipoServicioRequest request) {
        return TipoServicio.builder()
                .nombre(request.nombre())
                .costoBase(request.costoBase())
                .descripcion(request.descripcion())
                .cantidadMaxima(request.cantidadMaxima())
                .estado(request.estado())
                .build();
    }

    public void updateEntity(TipoServicio target, UpdateTipoServicioRequest request) {
        if (request.nombre() != null) target.setNombre(request.nombre());
        if (request.costoBase() != null) target.setCostoBase(request.costoBase());
        if (request.descripcion() != null) target.setDescripcion(request.descripcion());
        if (request.cantidadMaxima() != null) target.setCantidadMaxima(request.cantidadMaxima());
        if (request.estado() != null) target.setEstado(request.estado());
    }

    public TipoServicioResponse toResponse(TipoServicio entity) {
        return new TipoServicioResponse(
                entity.getTipoServicioId(),
                entity.getNombre(),
                entity.getCostoBase(),
                entity.getDescripcion(),
                entity.getCantidadMaxima(),
                entity.getEstado(),
                entity.getFechaCreacion(),
                entity.getFechaModificacion()
        );
    }
}
