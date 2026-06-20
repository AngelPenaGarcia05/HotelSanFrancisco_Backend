package com.sanfrancisco.api.modules.inventario.dto.request;

import com.sanfrancisco.api.shared.enums.EstadoActivo;
import jakarta.validation.constraints.Size;

public record UpdateCategoriaProductoRequest(

        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String nombre,

        @Size(max = 2000, message = "La descripción no puede exceder 2000 caracteres")
        String descripcion,

        EstadoActivo estado
) {
}
