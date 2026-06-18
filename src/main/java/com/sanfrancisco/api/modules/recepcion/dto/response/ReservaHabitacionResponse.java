package com.sanfrancisco.api.modules.recepcion.dto.response;

import com.sanfrancisco.api.modules.recepcion.enums.EstadoReservaHabitacion;

import java.math.BigDecimal;

public record ReservaHabitacionResponse(
        Integer reservaHabitacionId,
        Integer habitacionId,
        String habitacionNumero,
        Integer tipoHabitacionId,
        String tipoHabitacionNombre,
        BigDecimal tarifaPactada,
        Integer noches,
        BigDecimal subtotal,
        EstadoReservaHabitacion estado
) {
}
