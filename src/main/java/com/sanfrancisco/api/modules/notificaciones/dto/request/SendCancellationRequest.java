package com.sanfrancisco.api.modules.notificaciones.dto.request;

import jakarta.validation.constraints.NotNull;

public record SendCancellationRequest(
        @NotNull Integer reservaId,
        String motivo
) {
}
