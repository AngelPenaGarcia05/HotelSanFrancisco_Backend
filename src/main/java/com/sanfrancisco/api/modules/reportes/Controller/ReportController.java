package com.sanfrancisco.api.modules.reportes.Controller;

import com.sanfrancisco.api.shared.utils.DateTimeUtils;
import com.sanfrancisco.api.modules.reportes.dto.request.ExportReporteRequest;
import com.sanfrancisco.api.modules.reportes.dto.request.ReportRangeRequest;
import com.sanfrancisco.api.modules.reportes.dto.response.AttendanceReportResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.ManagementDashboardResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.OccupancyReportResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.PayrollReportResponse;
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

    @GetMapping("/forecast")
    public ApiResponse<OccupancyReportResponse> forecast(
            @RequestParam(name = "dias", defaultValue = "30") int dias) {
        return ApiResponse.ok(reportService.buildOccupancyForecast(dias));
    }

    @GetMapping("/nomina")
    public ApiResponse<PayrollReportResponse> nomina() {
        return ApiResponse.ok(reportService.buildPayrollReport());
    }

    @GetMapping("/asistencia")
    public ApiResponse<AttendanceReportResponse> asistencia(ReportRangeRequest range) {
        return ApiResponse.ok(reportService.buildAttendanceReport(range));
    }

    /**
     * Al igual que la nómina, la asistencia es información de personal: su
     * exportación va separada de /exportar y exige asistencia:read (ADMIN/RRHH).
     */
    @GetMapping("/asistencia/exportar")
    public ResponseEntity<byte[]> exportarAsistencia(
            @RequestParam(name = "formato", defaultValue = "PDF") String formato,
            ReportRangeRequest range) {
        byte[] contenido = reportService.exportarAsistencia(formato, range);
        String[] meta = switch (formato == null ? "PDF" : formato.trim().toUpperCase()) {
            case "CSV" -> new String[]{"csv", "text/csv; charset=UTF-8"};
            case "EXCEL" -> new String[]{"xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};
            default -> new String[]{"pdf", "application/pdf"};
        };
        String filename = "reporte-asistencia-" + DateTimeUtils.today() + "." + meta[0];
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(meta[1]));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(contenido);
    }

    /**
     * Exportación de nómina separada de /exportar a propósito: este endpoint
     * exige nomina:read (ADMIN/RRHH), mientras que /exportar solo exige
     * reporte:read y expondría información salarial a CAJA.
     */
    @GetMapping("/nomina/exportar")
    public ResponseEntity<byte[]> exportarNomina(
            @RequestParam(name = "formato", defaultValue = "PDF") String formato) {
        byte[] contenido = reportService.exportarNomina(formato);
        String[] meta = switch (formato == null ? "PDF" : formato.trim().toUpperCase()) {
            case "CSV" -> new String[]{"csv", "text/csv; charset=UTF-8"};
            case "EXCEL" -> new String[]{"xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};
            default -> new String[]{"pdf", "application/pdf"};
        };
        String filename = "reporte-nomina-" + DateTimeUtils.today() + "." + meta[0];
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(meta[1]));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(contenido);
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
