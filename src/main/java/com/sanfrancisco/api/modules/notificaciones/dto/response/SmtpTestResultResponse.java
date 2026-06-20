package com.sanfrancisco.api.modules.notificaciones.dto.response;

import java.time.LocalDateTime;

public record SmtpTestResultResponse(
        boolean success,
        String message,
        LocalDateTime sentAt
) {
}
