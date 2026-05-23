package com.sanfrancisco.api.modules.seguridad.dto.response;

import com.sanfrancisco.api.modules.seguridad.enums.EstadoSesion;

import java.time.LocalDateTime;

public record SesionResponse(
        Integer sesionId,
        String tokenHash,
        String ipOrigen,
        String userAgent,
        LocalDateTime fechaInicio,
        LocalDateTime fechaExpiracion,
        LocalDateTime fechaCierre,
        EstadoSesion estado,
        Integer usuarioId,
        String usuarioCorreo
) {
}
