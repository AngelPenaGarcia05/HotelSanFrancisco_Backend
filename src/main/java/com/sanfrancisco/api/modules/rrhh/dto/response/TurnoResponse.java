package com.sanfrancisco.api.modules.rrhh.dto.response;

import com.sanfrancisco.api.modules.rrhh.enums.EstadoTurno;
import com.sanfrancisco.api.modules.rrhh.enums.OrigenTurno;

import java.time.LocalDate;
import java.time.LocalTime;

public record TurnoResponse(
        Integer turnoId,
        Integer usuarioId,
        String usuarioNombreCompleto,
        Integer horarioId,
        String horarioNombreTurno,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        EstadoTurno estado,
        OrigenTurno origen
) {
}
