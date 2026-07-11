package com.sanfrancisco.api.modules.rrhh.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Solicita el cálculo (preview, no persiste) de la nómina de un empleado para un
 * periodo mensual, derivando descuentos desde la asistencia y sumando sus bonos.
 */
public record CalcularNominaRequest(
        @NotNull(message = "El usuario es obligatorio")
        Integer usuarioId,

        @NotBlank(message = "El periodo es obligatorio")
        @Pattern(regexp = "\\d{4}-\\d{2}", message = "El periodo debe tener formato YYYY-MM")
        String periodo
) {
}
