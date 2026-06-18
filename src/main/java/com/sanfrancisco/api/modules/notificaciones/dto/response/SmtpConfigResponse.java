package com.sanfrancisco.api.modules.notificaciones.dto.response;

import com.sanfrancisco.api.modules.notificaciones.enums.SmtpSecurity;

public record SmtpConfigResponse(
        String host,
        Integer puerto,
        String usuario,
        SmtpSecurity seguridad,
        String nombreRemitente,
        String correoRemitente,
        String responderA,
        Boolean habilitado
) {
}
