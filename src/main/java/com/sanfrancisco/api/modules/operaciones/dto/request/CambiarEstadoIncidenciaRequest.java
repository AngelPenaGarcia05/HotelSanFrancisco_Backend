package com.sanfrancisco.api.modules.operaciones.dto.request;

import com.sanfrancisco.api.modules.operaciones.enums.EstadoIncidencia;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CambiarEstadoIncidenciaRequest(

        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoIncidencia nuevoEstado,

        @Size(max = 2000, message = "La solución no puede exceder 2000 caracteres")
        String solucion
) {
}
