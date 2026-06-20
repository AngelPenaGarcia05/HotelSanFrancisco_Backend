package com.sanfrancisco.api.modules.rrhh.dto.request;

import com.sanfrancisco.api.modules.rrhh.enums.EstadoNomina;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoPagoNominaRequest(
        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoNomina nuevoEstado,
        
        String motivo
) {
}
