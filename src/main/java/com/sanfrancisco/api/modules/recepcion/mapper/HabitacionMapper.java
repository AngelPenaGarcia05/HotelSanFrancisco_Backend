package com.sanfrancisco.api.modules.recepcion.mapper;

import com.sanfrancisco.api.modules.recepcion.dto.request.CreateHabitacionRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.UpdateHabitacionRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.HabitacionResponse;
import com.sanfrancisco.api.modules.recepcion.entity.Habitacion;
import org.springframework.stereotype.Component;

@Component
public class HabitacionMapper {

    public Habitacion toEntity(CreateHabitacionRequest request) {
        return Habitacion.builder()
                .numero(request.numero().trim().toUpperCase())
                .piso(request.piso())
                .estado(request.estado())
                .descripcion(request.descripcion())
                .observaciones(request.observaciones())
                .build();
    }

    public void updateEntity(Habitacion target, UpdateHabitacionRequest request) {
        if (request.numero() != null && !request.numero().isBlank())
            target.setNumero(request.numero().trim().toUpperCase());
        if (request.piso() != null)
            target.setPiso(request.piso());
        if (request.estado() != null)
            target.setEstado(request.estado());
        if (request.descripcion() != null)
            target.setDescripcion(request.descripcion());
        if (request.observaciones() != null)
            target.setObservaciones(request.observaciones());
    }

    public HabitacionResponse toResponse(Habitacion entity) {
        return new HabitacionResponse(
                entity.getHabitacionId(),
                entity.getNumero(),
                entity.getPiso(),
                entity.getEstado(),
                entity.getDescripcion(),
                entity.getObservaciones(),
                entity.getFechaCreacion(),
                entity.getFechaModificacion()
        );
    }
}
