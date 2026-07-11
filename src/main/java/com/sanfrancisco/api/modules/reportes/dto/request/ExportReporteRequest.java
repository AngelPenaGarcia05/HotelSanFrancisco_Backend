package com.sanfrancisco.api.modules.reportes.dto.request;

import java.time.LocalDate;

public record ExportReporteRequest(
        String tipo,
        String formato,
        String period,
        String groupBy,
        LocalDate fechaInicio,
        LocalDate fechaFin
) {
    public ReportRangeRequest toRangeRequest() {
        return new ReportRangeRequest(period, groupBy, fechaInicio, fechaFin);
    }

    /** Formato de salida normalizado; CSV por defecto si no se especifica. */
    public String formatoNormalizado() {
        return (formato == null || formato.isBlank()) ? "CSV" : formato.trim().toUpperCase();
    }
}
