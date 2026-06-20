package com.sanfrancisco.api.modules.recepcion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelarReservaRequest(

        @NotBlank(message = "El motivo de cancelación es obligatorio")
        @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
        String motivo,

        /**
         * null o true  → se aplica la política automática según días de anticipación.
         * false        → recepción exonera la penalización (devolución total del adelanto).
         */
        Boolean aplicarPenalizacion
) {
}
