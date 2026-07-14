package com.sanfrancisco.api.modules.reportes.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Reporte de costo de nómina: totales del período consultado y desglose por
 * período de pago (formato YYYY-MM). Acceso restringido a nomina:read
 * (ADMIN/RRHH) por tratarse de información salarial.
 */
public record PayrollReportResponse(
        BigDecimal totalSueldoBase,
        BigDecimal totalBonos,
        BigDecimal totalDescuentos,
        BigDecimal totalNeto,
        long empleadosPagados,
        List<PayrollByPeriod> porPeriodo
) {

    public record PayrollByPeriod(
            String periodo,
            BigDecimal sueldoBase,
            BigDecimal bonos,
            BigDecimal descuentos,
            BigDecimal neto,
            long empleados
    ) {
    }
}
