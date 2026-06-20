package com.sanfrancisco.api.modules.seguridad.dto.response;

public record PermisoResponse(
        Integer permisoId,
        String nombre,
        String codigo
) {
}
