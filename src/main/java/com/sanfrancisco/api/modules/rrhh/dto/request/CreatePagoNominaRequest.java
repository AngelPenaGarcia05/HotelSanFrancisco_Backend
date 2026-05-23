package com.sanfrancisco.api.modules.rrhh.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreatePagoNominaRequest(
        @NotBlank(message = "El periodo es obligatorio")
        String periodo,

        @NotNull(message = "La fecha de emisión es obligatoria")
        LocalDate fechaEmision,

        @NotNull(message = "El sueldo base es obligatorio")
        @PositiveOrZero(message = "El sueldo base no puede ser negativo")
        BigDecimal sueldoBase,

        @NotNull(message = "El total de descuentos es obligatorio")
        @PositiveOrZero(message = "El total de descuentos no puede ser negativo")
        BigDecimal totalDescuentos,

        @NotNull(message = "El usuario es obligatorio")
        Integer usuarioId
) {
}
