package com.sanfrancisco.api.modules.recepcion.dto.request;

import jakarta.validation.constraints.NotNull;

public record CheckInRequest(

        @NotNull
        Integer reservaId,

        @NotNull
        Integer usuarioId,

        String observaciones
) {}
