package com.sanfrancisco.api.modules.reportes.dto.request;
import java.time.LocalDate;

/**
 * Filtro de período para los reportes. Si period = CUSTOM, fechaInicio/fechaFin son obligatorios.
 * groupBy determina la granularidad de la serie temporal (DAY/WEEK/MONTH).
 */
public record ReportRangeRequest(
        String period,
        String groupBy,
        LocalDate fechaInicio,
        LocalDate fechaFin
) {

    public ReportRangeRequest {
        if (period == null || period.isBlank()) period = "MONTH";
        if (groupBy == null || groupBy.isBlank()) groupBy = "DAY";
    }

    /** Resuelve el rango efectivo [desde, hasta] según el period seleccionado. */
    public LocalDate resolveDesde() {
        LocalDate hoy = LocalDate.now();
        return switch (period.toUpperCase()) {
            case "TODAY" -> hoy;
            case "WEEK" -> hoy.minusDays(6);
            case "QUARTER" -> hoy.minusMonths(3).plusDays(1);
            case "YEAR" -> hoy.minusYears(1).plusDays(1);
            case "CUSTOM" -> fechaInicio != null ? fechaInicio : hoy.minusMonths(1).plusDays(1);
            default -> hoy.minusMonths(1).plusDays(1); // MONTH
        };
    }

    public LocalDate resolveHasta() {
        if ("CUSTOM".equalsIgnoreCase(period) && fechaFin != null) return fechaFin;
        return LocalDate.now();
    }
}
