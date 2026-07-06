package com.sanfrancisco.api.modules.pagos.dto.request;

import com.sanfrancisco.api.modules.pagos.enums.TipoPago;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Filtros opcionales para búsqueda paginada/dinámica de pagos.
 * Cualquier campo null se ignora en la specification resultante.
 * Los rangos de fecha son días de calendario; los límites horarios
 * del día los fija el servidor (ver SpecificationUtils.dateTimeInDayRange).
 */
public record PagoFilterRequest(
        Integer metodoPagoId,
        TipoPago tipoPago,
        Integer ventaId,
        Integer reservaId,
        String comprobante,
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        BigDecimal montoMin,
        BigDecimal montoMax
) {
}
