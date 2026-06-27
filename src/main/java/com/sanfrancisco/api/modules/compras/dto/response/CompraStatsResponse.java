package com.sanfrancisco.api.modules.compras.dto.response;

import java.math.BigDecimal;

/**
 * Resumen agregado de compras para las tarjetas del frontend.
 * Los conteos respetan los filtros recibidos (incluido estado, si se envía).
 * montoTotalPeriodo SIEMPRE excluye las compras ANULADA (no son gasto real).
 */
public record CompraStatsResponse(
        long total,
        long pendientes,
        long recibidas,
        long anuladas,
        BigDecimal montoTotalPeriodo
) {
}
