package com.sanfrancisco.api.modules.recepcion.mapper;

import com.sanfrancisco.api.modules.recepcion.dto.request.CreateTipoHabitacionRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.UpdateTipoHabitacionRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.TipoHabitacionResponse;
import com.sanfrancisco.api.modules.recepcion.entity.TipoHabitacion;
import org.springframework.stereotype.Component;

@Component
public class TipoHabitacionMapper {

    public TipoHabitacion toEntity(CreateTipoHabitacionRequest request) {
        return TipoHabitacion.builder()
                .nombre(request.nombre())
                .precioBase(request.precioBase())
                .descripcion(request.descripcion())
                .estado(request.estado())
                .capacidadMaxima(request.capacidadMaxima())
                .build();
    }

    public void updateEntity(TipoHabitacion target, UpdateTipoHabitacionRequest request) {
        if (request.nombre() != null) target.setNombre(request.nombre());
        if (request.precioBase() != null) target.setPrecioBase(request.precioBase());
        if (request.descripcion() != null) target.setDescripcion(request.descripcion());
        if (request.estado() != null) target.setEstado(request.estado());
        if (request.capacidadMaxima() != null) target.setCapacidadMaxima(request.capacidadMaxima());
    }

    public TipoHabitacionResponse toResponse(TipoHabitacion entity) {
        return new TipoHabitacionResponse(
                entity.getTipoHabitacionId(),
                entity.getNombre(),
                entity.getPrecioBase(),
                entity.getDescripcion(),
                entity.getEstado(),
                entity.getCapacidadMaxima()
        );
    }
}
