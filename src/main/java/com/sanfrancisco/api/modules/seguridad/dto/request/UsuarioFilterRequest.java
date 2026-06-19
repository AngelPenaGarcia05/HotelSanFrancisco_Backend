package com.sanfrancisco.api.modules.seguridad.dto.request;

import com.sanfrancisco.api.modules.seguridad.enums.EstadoUsuario;

public record UsuarioFilterRequest(
        String nombre,
        String correo,
        EstadoUsuario estado,
        Integer rolId,
        Integer tipoDocumentoId,
        // ── Filtros laborales ──
        String cargo,
        String departamento,
        /** true → solo usuarios con rol staff (≠ CLIENTE); false → solo CLIENTE; null → todos */
        Boolean esEmpleado
) {
}
