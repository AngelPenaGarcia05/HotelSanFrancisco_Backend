package com.sanfrancisco.api.modules.inventario.dto.response;

import com.sanfrancisco.api.shared.enums.EstadoActivo;

import java.time.LocalDateTime;

public record CategoriaProductoResponse(
        Integer categoriaProductoId,
        String nombre,
        String descripcion,
        EstadoActivo estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {
}
