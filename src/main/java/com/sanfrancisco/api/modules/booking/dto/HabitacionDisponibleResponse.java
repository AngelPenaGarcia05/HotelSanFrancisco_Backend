package com.sanfrancisco.api.modules.booking.dto;

import java.math.BigDecimal;

public record HabitacionDisponibleResponse(
        Integer habitacionId,
        String numero,
        Integer piso,
        Integer tipoHabitacionId,
        String tipoHabitacionNombre,
        String descripcion,
        BigDecimal precioBase,
        Integer capacidadMaxima
) {}
