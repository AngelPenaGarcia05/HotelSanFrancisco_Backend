package com.sanfrancisco.api.modules.seguridad.dto.request;

import com.sanfrancisco.api.modules.seguridad.enums.EstadoSesion;

public record SesionFilterRequest(
        EstadoSesion estado,
        Integer usuarioId
) {
}
