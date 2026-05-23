package com.sanfrancisco.api.modules.inventario.dto.response;

import com.sanfrancisco.api.shared.enums.EstadoActivo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductoResponse(
        Integer productoId,
        String nombre,
        String descripcion,
        BigDecimal precioVenta,
        BigDecimal stockActual,
        BigDecimal stockMinimo,
        EstadoActivo estado,
        Integer categoriaProductoId,
        String categoriaProductoNombre,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {
}
