package com.sanfrancisco.api.modules.recepcion.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CheckOutRequest(

        @NotNull
        Integer reservaId,

        @NotNull
        Integer usuarioId,

        @PositiveOrZero
        BigDecimal consumosAdicionales,

        String observaciones
) {}
