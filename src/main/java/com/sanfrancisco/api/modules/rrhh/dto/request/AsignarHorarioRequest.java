package com.sanfrancisco.api.modules.rrhh.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AsignarHorarioRequest(
        @NotNull(message = "El horario es obligatorio")
        Integer horarioId,

        @NotNull(message = "El usuario es obligatorio")
        Integer usuarioId,

        @NotNull(message = "El día de la semana es obligatorio")
        @Min(value = 1, message = "El día de la semana debe ser entre 1 (Lunes) y 7 (Domingo)")
        @Max(value = 7, message = "El día de la semana debe ser entre 1 (Lunes) y 7 (Domingo)")
        Integer diaSemana,

        @NotNull(message = "La fecha de vigencia de inicio es obligatoria")
        LocalDate fechaVigenciaInicio,

        LocalDate fechaVigenciaFin
) {
}
