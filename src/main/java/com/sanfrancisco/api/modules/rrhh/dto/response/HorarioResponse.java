package com.sanfrancisco.api.modules.rrhh.dto.response;

import com.sanfrancisco.api.shared.enums.EstadoActivo;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record HorarioResponse(
        Integer horarioId,
        String nombreTurno,
        LocalTime horaEntrada,
        LocalTime horaSalida,
        EstadoActivo estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {
}
