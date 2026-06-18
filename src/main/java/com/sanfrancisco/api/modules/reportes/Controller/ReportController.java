package com.sanfrancisco.api.modules.reportes.Controller;

import com.sanfrancisco.api.modules.reportes.dto.request.ReportRangeRequest;
import com.sanfrancisco.api.modules.reportes.dto.response.ManagementDashboardResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.OccupancyReportResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.ReservationsReportResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.RevenueReportResponse;
import com.sanfrancisco.api.modules.reportes.service.interfaces.ReportService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reportes")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/ingresos")
    public ApiResponse<RevenueReportResponse> ingresos(ReportRangeRequest range) {
        return ApiResponse.ok(reportService.buildRevenueReport(range));
    }

    @GetMapping("/reservas")
    public ApiResponse<ReservationsReportResponse> reservas(ReportRangeRequest range) {
        return ApiResponse.ok(reportService.buildReservationsReport(range));
    }

    @GetMapping("/ocupacion")
    public ApiResponse<OccupancyReportResponse> ocupacion(ReportRangeRequest range) {
        return ApiResponse.ok(reportService.buildOccupancyReport(range));
    }

    @GetMapping("/gerencial")
    public ApiResponse<ManagementDashboardResponse> gerencial(ReportRangeRequest range) {
        return ApiResponse.ok(reportService.buildManagementDashboard(range));
    }
}
