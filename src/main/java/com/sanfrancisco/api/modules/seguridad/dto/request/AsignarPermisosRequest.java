package com.sanfrancisco.api.modules.seguridad.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AsignarPermisosRequest(
        @NotEmpty(message = "Debe especificar al menos un permiso")
        List<Integer> permisoIds
) {
}
