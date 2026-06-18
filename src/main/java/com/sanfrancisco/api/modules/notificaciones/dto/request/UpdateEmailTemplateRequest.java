package com.sanfrancisco.api.modules.notificaciones.dto.request;

public record UpdateEmailTemplateRequest(
        String asunto,
        String cuerpoHtml,
        Boolean activo
) {
}
