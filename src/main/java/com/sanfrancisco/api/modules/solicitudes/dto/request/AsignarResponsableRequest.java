package com.sanfrancisco.api.modules.solicitudes.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AsignarResponsableRequest(

        @NotNull(message = "El responsable es obligatorio")
        Integer responsableId,

        @Size(max = 1000, message = "La observación no puede exceder 1000 caracteres")
        String observacion
) {
}
