package com.sanfrancisco.api.modules.pagos.dto.response;

import java.math.BigDecimal;

/**
 * Resumen agregado de pagos de una reserva, para que el detalle muestre el
 * total pagado real (incluido el adelanto del flujo público) sin que el
 * frontend tenga que sumar los pagos individuales.
 * totalPagado = suma de pagos − reembolsos; saldoPendiente = montoTotal − totalPagado.
 */
public record ResumenPagosReservaResponse(
        Integer reservaId,
        BigDecimal montoTotal,
        BigDecimal adelanto,
        BigDecimal totalPagado,
        BigDecimal saldoPendiente
) {
}
