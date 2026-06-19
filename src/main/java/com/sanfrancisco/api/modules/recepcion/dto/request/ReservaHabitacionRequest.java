package com.sanfrancisco.api.modules.recepcion.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ReservaHabitacionRequest(

        @NotNull(message = "La habitación es obligatoria")
        Integer habitacionId,

        @NotNull(message = "El tipo de habitación es obligatorio")
        Integer tipoHabitacionId,

        // Si no se provee, se usa el precio base del tipo de habitación
        @PositiveOrZero(message = "La tarifa pactada no puede ser negativa")
        BigDecimal tarifaPactada
) {
}
