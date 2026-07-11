package com.sanfrancisco.api.modules.rrhh.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Genera turnos fechados a partir de la plantilla semanal (detalles_horario)
 * para el rango [desde, hasta] inclusive.
 */
public record GenerarTurnosRequest(
        @NotNull(message = "La fecha desde es obligatoria")
        LocalDate desde,

        @NotNull(message = "La fecha hasta es obligatoria")
        LocalDate hasta
) {
}
