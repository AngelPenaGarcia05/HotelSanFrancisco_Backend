package com.sanfrancisco.api.modules.rrhh.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateBonoRequest(
        @NotNull(message = "El monto es obligatorio")
        @PositiveOrZero(message = "El monto no puede ser negativo")
        BigDecimal monto,

        @NotBlank(message = "El motivo es obligatorio")
        @Size(max = 200, message = "El motivo no puede exceder los 200 caracteres")
        String motivo,

        @NotNull(message = "La fecha de asignación es obligatoria")
        LocalDate fechaAsignacion,

        @NotNull(message = "El usuario es obligatorio")
        Integer usuarioId,

        Integer pagoNominaId
) {
}
