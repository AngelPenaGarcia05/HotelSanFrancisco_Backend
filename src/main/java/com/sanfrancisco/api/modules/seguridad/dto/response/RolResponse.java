package com.sanfrancisco.api.modules.seguridad.dto.response;

import com.sanfrancisco.api.shared.enums.EstadoActivo;

import java.time.LocalDateTime;
import java.util.List;

public record RolResponse(
        Integer rolId,
        String nombre,
        String descripcion,
        EstadoActivo estado,
        List<PermisoResponse> permisos,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {
}
