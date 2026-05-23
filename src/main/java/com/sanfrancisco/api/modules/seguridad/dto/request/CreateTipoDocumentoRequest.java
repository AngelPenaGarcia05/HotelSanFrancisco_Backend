package com.sanfrancisco.api.modules.seguridad.dto.request;

import com.sanfrancisco.api.shared.enums.EstadoActivo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTipoDocumentoRequest(
        @NotBlank(message = "El acrónimo es obligatorio")
        @Size(max = 10, message = "El acrónimo no puede exceder 10 caracteres")
        String acronimo,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
        String nombre,

        @NotNull(message = "El estado es obligatorio")
        EstadoActivo estado
) {
}
