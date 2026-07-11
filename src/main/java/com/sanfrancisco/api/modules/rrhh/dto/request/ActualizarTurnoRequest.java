package com.sanfrancisco.api.modules.rrhh.dto.request;

import com.sanfrancisco.api.modules.rrhh.enums.EstadoTurno;

import java.time.LocalTime;

/**
 * Edición puntual de un turno (cobertura / excepción). Todos los campos son
 * opcionales; solo se aplican los presentes.
 */
public record ActualizarTurnoRequest(
        Integer horarioId,
        LocalTime horaInicio,
        LocalTime horaFin,
        EstadoTurno estado
) {
}
