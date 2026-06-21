package com.sanfrancisco.api.modules.seguridad.dto.response;

import java.util.List;

public record AuthUserResponse(
        Integer usuarioId,
        String nombre,
        String apellidoPaterno,
        String apellidoMaterno,
        String nombreCompleto,
        String correo,
        String rol,
        List<String> permisos,
        String direccion,
        String nacionalidad
) {}
