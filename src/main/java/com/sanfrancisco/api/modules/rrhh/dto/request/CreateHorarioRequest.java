package com.sanfrancisco.api.modules.rrhh.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record CreateHorarioRequest(
        @NotBlank(message = "El nombre del turno es obligatorio")
        @Size(max = 80, message = "El nombre del turno no puede exceder los 80 caracteres")
        String nombreTurno,

        @NotNull(message = "La hora de entrada es obligatoria")
        LocalTime horaEntrada,

        @NotNull(message = "La hora de salida es obligatoria")
        LocalTime horaSalida
) {
}
