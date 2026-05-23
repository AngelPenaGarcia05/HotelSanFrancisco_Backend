package com.sanfrancisco.api.modules.seguridad.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "El token de refresco es requerido")
        String refreshToken
) {}
