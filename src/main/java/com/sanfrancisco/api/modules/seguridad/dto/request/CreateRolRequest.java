package com.sanfrancisco.api.modules.seguridad.dto.request;

import com.sanfrancisco.api.shared.enums.EstadoActivo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateRolRequest(
        @NotBlank(message = "El nombre del rol es obligatorio")
        @Size(max = 80, message = "El nombre del rol no puede exceder 80 caracteres")
        String nombre,

        String descripcion,

        @NotNull(message = "El estado es obligatorio")
        EstadoActivo estado,

        List<Integer> permisoIds
) {
}
