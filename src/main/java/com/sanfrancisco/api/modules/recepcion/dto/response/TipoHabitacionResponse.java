package com.sanfrancisco.api.modules.recepcion.dto.response;

import com.sanfrancisco.api.shared.enums.EstadoActivo;

import java.math.BigDecimal;

public record TipoHabitacionResponse(
        Integer tipoHabitacionId,
        String nombre,
        BigDecimal precioBase,
        String descripcion,
        EstadoActivo estado,
        Integer capacidadMaxima
) {
}
