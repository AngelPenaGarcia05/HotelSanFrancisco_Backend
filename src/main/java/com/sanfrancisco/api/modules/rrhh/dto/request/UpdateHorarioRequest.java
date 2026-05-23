package com.sanfrancisco.api.modules.rrhh.dto.request;

import com.sanfrancisco.api.shared.enums.EstadoActivo;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record UpdateHorarioRequest(
        @Size(max = 80, message = "El nombre del turno no puede exceder los 80 caracteres")
        String nombreTurno,

        LocalTime horaEntrada,

        LocalTime horaSalida,

        EstadoActivo estado
) {
}
