package com.sanfrancisco.api.modules.notificaciones.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateReminderSettingsRequest(
        @NotNull @Min(1) @Max(72) Integer horasAntesCheckIn,
        @NotNull Boolean habilitado,
        @NotBlank String horaEnvio
) {
}

