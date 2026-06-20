package com.sanfrancisco.api.modules.seguridad.dto.response;

import java.time.Instant;

public record LoginResponse(
        boolean success,
        String message,
        AuthUserResponse user,
        Instant timestamp
) {}
