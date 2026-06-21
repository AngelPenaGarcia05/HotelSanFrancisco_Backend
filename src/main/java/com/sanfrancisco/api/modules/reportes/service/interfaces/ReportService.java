package com.sanfrancisco.api.modules.reportes.service.interfaces;

import com.sanfrancisco.api.modules.reportes.dto.request.ExportReporteRequest;
import com.sanfrancisco.api.modules.reportes.dto.request.ReportRangeRequest;
import com.sanfrancisco.api.modules.reportes.dto.response.ManagementDashboardResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.OccupancyReportResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.ReservationsReportResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.RevenueReportResponse;

public interface ReportService {

    RevenueReportResponse buildRevenueReport(ReportRangeRequest range);

    ReservationsReportResponse buildReservationsReport(ReportRangeRequest range);

    OccupancyReportResponse buildOccupancyReport(ReportRangeRequest range);

    ManagementDashboardResponse buildManagementDashboard(ReportRangeRequest range);

    byte[] exportar(ExportReporteRequest request);
}
