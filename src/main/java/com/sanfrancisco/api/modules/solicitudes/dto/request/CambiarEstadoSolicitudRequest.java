package com.sanfrancisco.api.modules.solicitudes.dto.request;

import com.sanfrancisco.api.modules.solicitudes.enums.EstadoSolicitud;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CambiarEstadoSolicitudRequest(

        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoSolicitud nuevoEstado,

        @Size(max = 1000, message = "La observación no puede exceder 1000 caracteres")
        String observacion
) {
}
