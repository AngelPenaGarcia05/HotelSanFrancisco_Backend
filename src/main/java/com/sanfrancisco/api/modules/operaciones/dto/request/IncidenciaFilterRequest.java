package com.sanfrancisco.api.modules.operaciones.dto.request;

import com.sanfrancisco.api.modules.operaciones.enums.EstadoIncidencia;
import com.sanfrancisco.api.modules.operaciones.enums.PrioridadIncidencia;

import java.time.LocalDate;

/**
 * Filtros opcionales para búsqueda paginada/dinámica de incidencias.
 * Cualquier campo null se ignora en la specification resultante.
 * Los rangos de fecha son días de calendario; los límites horarios
 * del día los fija el servidor (ver SpecificationUtils.dateTimeInDayRange).
 */
public record IncidenciaFilterRequest(
        EstadoIncidencia estado,
        PrioridadIncidencia prioridad,
        Integer usuarioId,
        Integer reservaHabitacionId,
        LocalDate fechaReporteDesde,
        LocalDate fechaReporteHasta
) {
}
