package com.sanfrancisco.api.modules.operaciones.dto.response;

import com.sanfrancisco.api.modules.operaciones.enums.EstadoIncidencia;
import com.sanfrancisco.api.modules.operaciones.enums.PrioridadIncidencia;

import java.time.LocalDateTime;

public record IncidenciaResponse(
        Integer incidenciaId,
        String descripcion,
        LocalDateTime fechaReporte,
        LocalDateTime fechaResolucion,
        PrioridadIncidencia prioridad,
        String solucion,
        EstadoIncidencia estado,
        Integer usuarioId,
        String usuarioNombre,
        Integer reservaHabitacionId,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {
}
