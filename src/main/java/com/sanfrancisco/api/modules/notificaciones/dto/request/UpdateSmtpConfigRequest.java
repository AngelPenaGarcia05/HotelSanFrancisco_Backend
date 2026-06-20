package com.sanfrancisco.api.modules.notificaciones.dto.request;

import com.sanfrancisco.api.modules.notificaciones.enums.SmtpSecurity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateSmtpConfigRequest(
        @NotBlank String host,
        @NotNull @Min(1) Integer puerto,
        @NotBlank String usuario,
        String password,
        @NotNull SmtpSecurity seguridad,
        @NotBlank String nombreRemitente,
        @NotBlank @Email String correoRemitente,
        @Email String responderA,
        @NotNull Boolean habilitado) {
}