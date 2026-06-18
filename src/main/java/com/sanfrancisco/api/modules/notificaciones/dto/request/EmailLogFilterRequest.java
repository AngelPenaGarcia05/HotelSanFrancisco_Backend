package com.sanfrancisco.api.modules.notificaciones.dto.request;

import com.sanfrancisco.api.modules.notificaciones.enums.EmailStatus;
import com.sanfrancisco.api.modules.notificaciones.enums.EmailTemplateKey;

public record EmailLogFilterRequest(
        String search,
        EmailStatus estado,
        EmailTemplateKey plantilla
) {
}
