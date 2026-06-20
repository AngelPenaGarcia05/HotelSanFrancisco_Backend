package com.sanfrancisco.api.modules.operaciones.dto.request;

import com.sanfrancisco.api.modules.operaciones.enums.EstadoIncidencia;
import com.sanfrancisco.api.modules.operaciones.enums.PrioridadIncidencia;

import java.time.LocalDateTime;

/**
 * Filtros opcionales para búsqueda paginada/dinámica de incidencias.
 * Cualquier campo null se ignora en la specification resultante.
 */
public record IncidenciaFilterRequest(
        EstadoIncidencia estado,
        PrioridadIncidencia prioridad,
        Integer usuarioId,
        Integer reservaHabitacionId,
        LocalDateTime fechaReporteDesde,
        LocalDateTime fechaReporteHasta
) {
}
