package com.sanfrancisco.api.modules.pagos.dto.request;

import com.sanfrancisco.api.modules.pagos.enums.TipoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Filtros opcionales para búsqueda paginada/dinámica de pagos.
 * Cualquier campo null se ignora en la specification resultante.
 */
public record PagoFilterRequest(
        Integer metodoPagoId,
        TipoPago tipoPago,
        Integer ventaId,
        Integer reservaId,
        String comprobante,
        LocalDateTime fechaDesde,
        LocalDateTime fechaHasta,
        BigDecimal montoMin,
        BigDecimal montoMax
) {
}
