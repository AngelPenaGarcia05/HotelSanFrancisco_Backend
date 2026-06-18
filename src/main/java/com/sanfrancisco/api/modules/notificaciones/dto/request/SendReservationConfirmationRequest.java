package com.sanfrancisco.api.modules.notificaciones.dto.request;

import jakarta.validation.constraints.NotNull;

public record SendReservationConfirmationRequest(
        @NotNull Integer reservaId
) {
}
