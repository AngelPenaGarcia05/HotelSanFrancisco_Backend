package com.sanfrancisco.api.modules.seguridad.dto.request;

import com.sanfrancisco.api.modules.seguridad.enums.EstadoUsuario;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoUsuarioRequest(
        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoUsuario nuevoEstado
) {
}
