package com.sanfrancisco.api.modules.reportes.service.interfaces;

import com.sanfrancisco.api.modules.reportes.dto.request.ExportReporteRequest;
import com.sanfrancisco.api.modules.reportes.dto.request.ReportRangeRequest;
import com.sanfrancisco.api.modules.reportes.dto.response.AttendanceReportResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.ManagementDashboardResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.OccupancyReportResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.PayrollReportResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.ReservationsReportResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.RevenueReportResponse;

public interface ReportService {

    RevenueReportResponse buildRevenueReport(ReportRangeRequest range);

    ReservationsReportResponse buildReservationsReport(ReportRangeRequest range);

    OccupancyReportResponse buildOccupancyReport(ReportRangeRequest range);

    ManagementDashboardResponse buildManagementDashboard(ReportRangeRequest range);

    /** Ocupación proyectada para los próximos {@code dias} días (1..90). */
    OccupancyReportResponse buildOccupancyForecast(int dias);

    /** Costo de nómina agrupado por período (restringido a nomina:read). */
    PayrollReportResponse buildPayrollReport();

    /** Exporta el reporte de nómina en CSV/EXCEL/PDF (restringido a nomina:read). */
    byte[] exportarNomina(String formato);

    /** Ausentismo y puntualidad por empleado (restringido a asistencia:read). */
    AttendanceReportResponse buildAttendanceReport(ReportRangeRequest range);

    /** Exporta el reporte de asistencia en CSV/EXCEL/PDF (restringido a asistencia:read). */
    byte[] exportarAsistencia(String formato, ReportRangeRequest range);

    byte[] exportar(ExportReporteRequest request);
}
