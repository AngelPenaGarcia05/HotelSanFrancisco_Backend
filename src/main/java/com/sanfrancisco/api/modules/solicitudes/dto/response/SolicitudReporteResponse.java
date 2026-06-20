package com.sanfrancisco.api.modules.solicitudes.dto.response;

import com.sanfrancisco.api.modules.solicitudes.enums.EstadoSolicitud;
import com.sanfrancisco.api.modules.solicitudes.enums.TipoSolicitud;

import java.util.Map;

/**
 * Reporte consolidado de solicitudes: totales y desgloses por estado y tipo.
 */
public record SolicitudReporteResponse(
        long total,
        Map<EstadoSolicitud, Long> porEstado,
        Map<TipoSolicitud, Long> porTipo,
        long pendientes,
        long cerradas
) {
}
