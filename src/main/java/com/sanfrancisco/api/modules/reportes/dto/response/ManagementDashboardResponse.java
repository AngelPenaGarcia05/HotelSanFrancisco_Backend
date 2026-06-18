package com.sanfrancisco.api.modules.reportes.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ManagementDashboardResponse(
        LocalDateTime generadoEn,
        ManagementKpis kpis,
        RevenueReportResponse ingresos,
        ReservationsReportResponse reservas,
        OccupancyReportResponse ocupacion
) {

    public record ManagementKpis(
            BigDecimal ingresosMesActual,
            BigDecimal ingresosMesAnterior,
            BigDecimal variacionIngresos,
            BigDecimal ocupacionActual,
            BigDecimal ocupacionMesAnterior,
            BigDecimal variacionOcupacion,
            long reservasActivas,
            long reservasPendientesPago,
            BigDecimal adrActual,
            BigDecimal revparActual,
            long cancelacionesMes
    ) {
    }
}
