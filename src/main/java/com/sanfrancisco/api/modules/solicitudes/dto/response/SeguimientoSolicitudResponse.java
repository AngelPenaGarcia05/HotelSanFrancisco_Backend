package com.sanfrancisco.api.modules.solicitudes.dto.response;

import com.sanfrancisco.api.modules.solicitudes.enums.AccionSeguimiento;
import com.sanfrancisco.api.modules.solicitudes.enums.EstadoSolicitud;

import java.time.LocalDateTime;

public record SeguimientoSolicitudResponse(
        Integer seguimientoId,
        Integer solicitudId,
        LocalDateTime fechaAccion,
        AccionSeguimiento accion,
        EstadoSolicitud estadoAnterior,
        EstadoSolicitud estadoNuevo,
        String observacion,
        Integer responsableId,
        String responsableNombre
) {
}
