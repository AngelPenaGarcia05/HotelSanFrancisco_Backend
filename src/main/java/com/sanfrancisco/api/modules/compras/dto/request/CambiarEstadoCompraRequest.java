package com.sanfrancisco.api.modules.compras.dto.request;

import com.sanfrancisco.api.modules.compras.enums.EstadoCompra;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CambiarEstadoCompraRequest(

        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoCompra nuevoEstado,

        @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
        String motivo
) {
}
