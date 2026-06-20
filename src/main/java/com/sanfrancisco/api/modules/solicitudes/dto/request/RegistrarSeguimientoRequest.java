package com.sanfrancisco.api.modules.solicitudes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Añade una observación al historial de una solicitud sin cambiar su estado.
 */
public record RegistrarSeguimientoRequest(

        @NotBlank(message = "La observación es obligatoria")
        @Size(max = 1000, message = "La observación no puede exceder 1000 caracteres")
        String observacion
) {
}
