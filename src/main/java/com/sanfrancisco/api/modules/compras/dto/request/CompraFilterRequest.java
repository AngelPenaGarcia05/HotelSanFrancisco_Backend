package com.sanfrancisco.api.modules.compras.dto.request;

import com.sanfrancisco.api.modules.compras.enums.EstadoCompra;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Filtros opcionales para búsqueda paginada/dinámica de compras.
 * Cualquier campo null se ignora en la specification resultante.
 */
public record CompraFilterRequest(
        String numeroFactura,
        EstadoCompra estado,
        Integer proveedorId,
        LocalDate fechaCompraDesde,
        LocalDate fechaCompraHasta,
        BigDecimal montoTotalMin,
        BigDecimal montoTotalMax
) {
}
