package com.sanfrancisco.api.modules.seguridad.dto.request;

import com.sanfrancisco.api.shared.enums.EstadoActivo;
import jakarta.validation.constraints.Size;

public record UpdateTipoDocumentoRequest(
        @Size(max = 10, message = "El acrónimo no puede exceder 10 caracteres")
        String acronimo,

        @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
        String nombre,

        EstadoActivo estado
) {
}
