package com.sanfrancisco.api.modules.reportes.dto.request;

import java.time.LocalDate;

public record ExportReporteRequest(
        String tipo,
        String period,
        String groupBy,
        LocalDate fechaInicio,
        LocalDate fechaFin
) {
    public ReportRangeRequest toRangeRequest() {
        return new ReportRangeRequest(period, groupBy, fechaInicio, fechaFin);
    }
}
