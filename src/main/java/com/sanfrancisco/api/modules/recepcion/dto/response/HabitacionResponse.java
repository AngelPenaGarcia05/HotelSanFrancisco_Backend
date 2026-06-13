package com.sanfrancisco.api.modules.recepcion.dto.response;

import com.sanfrancisco.api.modules.recepcion.enums.EstadoHabitacion;

import java.time.LocalDateTime;

public record HabitacionResponse(
        Integer habitacionId,
        String numero,
        Integer piso,
        EstadoHabitacion estado,
        String descripcion,
        String observaciones,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {}
