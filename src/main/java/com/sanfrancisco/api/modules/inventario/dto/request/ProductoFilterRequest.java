package com.sanfrancisco.api.modules.inventario.dto.request;

import com.sanfrancisco.api.shared.enums.EstadoActivo;

import java.math.BigDecimal;

/**
 * Filtros opcionales para búsqueda paginada/dinámica de productos.
 * Cualquier campo null se ignora en la specification resultante.
 */
public record ProductoFilterRequest(
        String nombre,
        EstadoActivo estado,
        Integer categoriaProductoId,
        BigDecimal precioVentaMin,
        BigDecimal precioVentaMax,
        Boolean bajoStock
) {
}
