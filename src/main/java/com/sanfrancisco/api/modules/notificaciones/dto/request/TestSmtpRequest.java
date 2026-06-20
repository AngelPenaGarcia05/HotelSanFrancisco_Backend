package com.sanfrancisco.api.modules.notificaciones.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record TestSmtpRequest(
        @NotBlank @Email String destinatario
) {
}
