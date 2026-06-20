package com.sanfrancisco.api.modules.seguridad.dto.request;

import com.sanfrancisco.api.shared.enums.EstadoActivo;

public record RolFilterRequest(
        String nombre,
        EstadoActivo estado
) {
}
