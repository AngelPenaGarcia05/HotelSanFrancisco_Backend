package com.sanfrancisco.api.modules.reportes.Controller;

import com.sanfrancisco.api.shared.utils.DateTimeUtils;
import com.sanfrancisco.api.modules.reportes.dto.request.ExportReporteRequest;
import com.sanfrancisco.api.modules.reportes.dto.request.ReportRangeRequest;
import com.sanfrancisco.api.modules.reportes.dto.response.ManagementDashboardResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.OccupancyReportResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.ReservationsReportResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.RevenueReportResponse;
import com.sanfrancisco.api.modules.reportes.service.interfaces.ReportService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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

    @PostMapping("/exportar")
    public ResponseEntity<byte[]> exportar(@RequestBody ExportReporteRequest request) {
        byte[] contenido = reportService.exportar(request);
        String[] meta = switch (request.formatoNormalizado()) {
            case "PDF" -> new String[]{"pdf", "application/pdf"};
            case "EXCEL" -> new String[]{"xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};
            default -> new String[]{"csv", "text/csv; charset=UTF-8"};
        };
        String filename = "reporte-" + (request.tipo() != null ? request.tipo() : "general")
                + "-" + DateTimeUtils.today() + "." + meta[0];
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(meta[1]));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(contenido);
    }
}
