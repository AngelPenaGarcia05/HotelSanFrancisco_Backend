package com.sanfrancisco.api.modules.solicitudes.dto.request;

import com.sanfrancisco.api.modules.solicitudes.enums.EstadoSolicitud;
import com.sanfrancisco.api.modules.solicitudes.enums.ModuloReferido;
import com.sanfrancisco.api.modules.solicitudes.enums.PrioridadSolicitud;
import com.sanfrancisco.api.modules.solicitudes.enums.TipoSolicitud;

import java.time.LocalDate;

/**
 * Filtros opcionales para la búsqueda paginada de solicitudes.
 * Todos los campos son nullable; se ignoran los que no se envíen.
 * Los rangos de fecha son días de calendario; los límites horarios
 * del día los fija el servidor (ver SpecificationUtils.dateTimeInDayRange).
 */
public record SolicitudFilterRequest(
        EstadoSolicitud estado,
        TipoSolicitud tipoSolicitud,
        PrioridadSolicitud prioridad,
        ModuloReferido moduloReferido,
        Integer solicitanteId,
        Integer responsableId,
        LocalDate fechaRegistroDesde,
        LocalDate fechaRegistroHasta
) {
}
