package com.sanfrancisco.api.modules.recepcion.dto.response;

import com.sanfrancisco.api.modules.recepcion.enums.EstadoHabitacion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HabitacionResponse(
        Integer habitacionId,
        String numero,
        Integer piso,
        EstadoHabitacion estado,
        String descripcion,
        String observaciones,
        Integer tipoHabitacionId,
        String tipoHabitacionNombre,
        BigDecimal precioBase,
        Integer capacidadMaxima,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {}
