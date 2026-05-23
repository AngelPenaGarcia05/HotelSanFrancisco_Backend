package com.sanfrancisco.api.modules.recepcion.dto.request;

import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CambiarEstadoReservaRequest(

        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoReserva nuevoEstado,

        @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
        String motivo
) {
}
