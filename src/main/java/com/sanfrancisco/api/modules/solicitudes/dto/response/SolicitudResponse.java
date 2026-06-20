package com.sanfrancisco.api.modules.solicitudes.dto.response;

import com.sanfrancisco.api.modules.solicitudes.enums.EstadoSolicitud;
import com.sanfrancisco.api.modules.solicitudes.enums.ModuloReferido;
import com.sanfrancisco.api.modules.solicitudes.enums.PrioridadSolicitud;
import com.sanfrancisco.api.modules.solicitudes.enums.TipoAcceso;
import com.sanfrancisco.api.modules.solicitudes.enums.TipoSolicitud;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SolicitudResponse(
        Integer solicitudId,
        String codigoSolicitud,
        LocalDateTime fechaRegistro,
        TipoSolicitud tipoSolicitud,
        String asunto,
        String descripcion,
        PrioridadSolicitud prioridad,
        ModuloReferido moduloReferido,
        EstadoSolicitud estado,
        String observaciones,
        LocalDateTime fechaCierre,
        String rolSolicitado,
        TipoAcceso tipoAcceso,
        LocalDate periodoInicio,
        LocalDate periodoFin,
        Integer solicitanteId,
        String solicitanteNombre,
        Integer responsableId,
        String responsableNombre,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {
}
