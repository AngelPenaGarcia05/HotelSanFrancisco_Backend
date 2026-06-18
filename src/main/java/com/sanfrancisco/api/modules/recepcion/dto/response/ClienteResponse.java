package com.sanfrancisco.api.modules.recepcion.dto.response;

import com.sanfrancisco.api.shared.enums.EstadoActivo;

import java.time.LocalDateTime;

public record ClienteResponse(
        Integer huespedId,
        String nombre,
        String apellidoPaterno,
        String apellidoMaterno,
        String nombreCompleto,
        String numeroDocumento,
        String nacionalidad,
        String correo,
        String telefono,
        EstadoActivo estado,
        Integer usuarioId,
        String usuarioNombre,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {
}
