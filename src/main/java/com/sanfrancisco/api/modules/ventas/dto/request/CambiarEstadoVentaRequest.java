package com.sanfrancisco.api.modules.ventas.dto.request;

import com.sanfrancisco.api.modules.ventas.enums.EstadoVenta;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CambiarEstadoVentaRequest(

        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoVenta nuevoEstado,

        @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
        String motivo
) {
}
