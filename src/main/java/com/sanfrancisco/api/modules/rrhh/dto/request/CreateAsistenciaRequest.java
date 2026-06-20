package com.sanfrancisco.api.modules.rrhh.dto.request;

import com.sanfrancisco.api.modules.rrhh.enums.TipoAsistencia;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateAsistenciaRequest(
        @NotNull(message = "La fecha es obligatoria")
        LocalDate fecha,

        @NotNull(message = "La hora de ingreso es obligatoria")
        LocalTime horaIngreso,

        @NotNull(message = "El tipo de asistencia es obligatorio")
        TipoAsistencia tipo,

        String observaciones,

        @NotNull(message = "El usuario es obligatorio")
        Integer usuarioId
) {
}
