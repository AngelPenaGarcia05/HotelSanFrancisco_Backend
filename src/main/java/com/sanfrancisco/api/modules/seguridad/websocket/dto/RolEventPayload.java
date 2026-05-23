package com.sanfrancisco.api.modules.seguridad.websocket.dto;

import com.sanfrancisco.api.shared.enums.EstadoActivo;

public record RolEventPayload(
        Integer rolId,
        String nombre,
        EstadoActivo estado
) {
}
