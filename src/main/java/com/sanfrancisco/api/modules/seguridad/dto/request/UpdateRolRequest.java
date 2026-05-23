package com.sanfrancisco.api.modules.seguridad.dto.request;

import com.sanfrancisco.api.shared.enums.EstadoActivo;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateRolRequest(
        @Size(max = 80, message = "El nombre del rol no puede exceder 80 caracteres")
        String nombre,

        String descripcion,

        EstadoActivo estado,

        List<Integer> permisoIds
) {
}
