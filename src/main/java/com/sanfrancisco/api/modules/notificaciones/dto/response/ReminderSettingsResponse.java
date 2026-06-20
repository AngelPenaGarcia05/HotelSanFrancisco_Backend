package com.sanfrancisco.api.modules.notificaciones.dto.response;

public record ReminderSettingsResponse(
        Integer horasAntesCheckIn,
        Boolean habilitado,
        String horaEnvio
) {
}
