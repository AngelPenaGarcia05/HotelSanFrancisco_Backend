package com.sanfrancisco.api.modules.notificaciones.dto.response;

import com.sanfrancisco.api.modules.notificaciones.enums.EmailStatus;
import com.sanfrancisco.api.modules.notificaciones.enums.EmailTemplateKey;

import java.time.LocalDateTime;

public record EmailLogResponse(
        Integer id,
        String destinatario,
        String asunto,
        EmailTemplateKey plantilla,
        EmailStatus estado,
        Integer reservaId,
        String codReserva,
        Integer pagoId,
        LocalDateTime enviadoEn,
        String error,
        Integer intentos
) {
}
