package com.sanfrancisco.api.modules.rrhh.dto.response;

import com.sanfrancisco.api.shared.enums.EstadoActivo;

import java.time.LocalDate;

public record DetalleHorarioResponse(
        Integer usuarioId,
        String usuarioNombreCompleto,
        Integer horarioId,
        String horarioNombreTurno,
        Integer diaSemana,
        EstadoActivo estado,
        LocalDate fechaVigenciaInicio,
        LocalDate fechaVigenciaFin
) {
}
