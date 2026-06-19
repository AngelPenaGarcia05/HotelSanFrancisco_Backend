package com.sanfrancisco.api.modules.seguridad.dto.request;

import jakarta.validation.constraints.NotNull;

public record CambiarRolUsuarioRequest(
        @NotNull(message = "El ID del rol es obligatorio")
        Integer rolId
) {
}
