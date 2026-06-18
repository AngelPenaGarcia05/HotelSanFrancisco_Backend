package com.sanfrancisco.api.modules.reportes.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RevenueReportResponse(
        BigDecimal totalIngresos,
        BigDecimal totalAnticipos,
        BigDecimal totalSaldos,
        BigDecimal totalReembolsos,
        BigDecimal ingresoPromedioDiario,
        List<RevenuePoint> serie,
        List<RevenueByMethod> porMetodoPago
) {

    public record RevenuePoint(
            LocalDate fecha,
            BigDecimal ingresosAnticipos,
            BigDecimal ingresosSaldos,
            BigDecimal reembolsos
    ) {
    }

    public record RevenueByMethod(
            String metodoPago,
            BigDecimal monto,
            BigDecimal porcentaje
    ) {
    }
}