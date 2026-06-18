package com.sanfrancisco.api.modules.notificaciones.dto.response;

import com.sanfrancisco.api.modules.notificaciones.enums.EmailTemplateKey;

import java.util.List;

public record EmailTemplateResponse(
        EmailTemplateKey clave,
        String nombre,
        String asunto,
        String cuerpoHtml,
        Boolean activo,
        List<String> variables
) {
}
