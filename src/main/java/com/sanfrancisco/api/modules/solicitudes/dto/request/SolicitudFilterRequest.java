package com.sanfrancisco.api.modules.solicitudes.dto.request;

import com.sanfrancisco.api.modules.solicitudes.enums.EstadoSolicitud;
import com.sanfrancisco.api.modules.solicitudes.enums.ModuloReferido;
import com.sanfrancisco.api.modules.solicitudes.enums.PrioridadSolicitud;
import com.sanfrancisco.api.modules.solicitudes.enums.TipoSolicitud;

import java.time.LocalDateTime;

/**
 * Filtros opcionales para la búsqueda paginada de solicitudes.
 * Todos los campos son nullable; se ignoran los que no se envíen.
 */
public record SolicitudFilterRequest(
        EstadoSolicitud estado,
        TipoSolicitud tipoSolicitud,
        PrioridadSolicitud prioridad,
        ModuloReferido moduloReferido,
        Integer solicitanteId,
        Integer responsableId,
        LocalDateTime fechaRegistroDesde,
        LocalDateTime fechaRegistroHasta
) {
}
