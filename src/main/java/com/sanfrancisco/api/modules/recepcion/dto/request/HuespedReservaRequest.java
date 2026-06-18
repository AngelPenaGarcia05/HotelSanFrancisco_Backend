package com.sanfrancisco.api.modules.recepcion.dto.request;

import jakarta.validation.constraints.NotNull;

public record HuespedReservaRequest(

        @NotNull(message = "El huésped es obligatorio")
        Integer huespedId,

        @NotNull(message = "Debe indicar si el huésped es el principal")
        Boolean esPrincipal
) {
}
