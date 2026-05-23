package com.sanfrancisco.api.modules.seguridad.websocket.dto;

import com.sanfrancisco.api.modules.seguridad.enums.EstadoUsuario;

public record UsuarioEventPayload(
        Integer usuarioId,
        String nombreCompleto,
        String correo,
        EstadoUsuario estado,
        String rolNombre
) {
}
