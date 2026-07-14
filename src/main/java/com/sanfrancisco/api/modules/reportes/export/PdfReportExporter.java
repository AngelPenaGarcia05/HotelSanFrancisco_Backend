package com.sanfrancisco.api.modules.reportes.export;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.sanfrancisco.api.modules.reportes.dto.response.AttendanceReportResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.ManagementDashboardResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.OccupancyReportResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.PayrollReportResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.ReservationsReportResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.RevenueReportResponse;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import com.sanfrancisco.api.modules.seguridad.security.CustomUserDetails;
import com.sanfrancisco.api.modules.seguridad.security.UserPrincipal;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Genera PDFs de los reportes: Thymeleaf arma el HTML a partir de los DTOs y
 * OpenHTMLtoPDF lo convierte (HTML + CSS -> PDF). Encabezado común a todos los
 * reportes: logo, título, fecha de generación y usuario que lo generó.
 */
@Component
public class PdfReportExporter {

    public static final String CONTENT_TYPE = "application/pdf";
    public static final String EXTENSION = "pdf";

    private static final Locale ES_PE = Locale.forLanguageTag("es-PE");
    private static final DateTimeFormatter FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Logo del hotel embebido como data-URI (se lee del classpath una sola vez). */
    private static final String LOGO_DATA_URI = cargarLogo();

    private final TemplateEngine templateEngine;
    private final ChartImageService chartService;
    private final UsuarioRepository usuarioRepository;

    public PdfReportExporter(TemplateEngine reportesTemplateEngine,
                             ChartImageService chartService,
                             UsuarioRepository usuarioRepository) {
        this.templateEngine = reportesTemplateEngine;
        this.chartService = chartService;
        this.usuarioRepository = usuarioRepository;
    }

    public byte[] gerencial(ManagementDashboardResponse r) {
        Context ctx = baseContext(r.generadoEn());
        ctx.setVariable("kpis", r.kpis());
        ctx.setVariable("ingresos", r.ingresos());
        ctx.setVariable("reservas", r.reservas());
        ctx.setVariable("ocupacion", r.ocupacion());
        graficosIngresos(ctx, r.ingresos());
        graficosReservas(ctx, r.reservas());
        graficosOcupacion(ctx, r.ocupacion());
        return render(templateEngine.process("reportes/gerencial", ctx));
    }

    public byte[] ingresos(RevenueReportResponse r) {
        Context ctx = baseContext(LocalDateTime.now());
        ctx.setVariable("ingresos", r);
        graficosIngresos(ctx, r);
        ctx.setVariable("chartIngresosDiarios",
                r.serie().isEmpty() ? null : chartService.ingresosDiarios(r.serie()));
        return render(templateEngine.process("reportes/ingresos", ctx));
    }

    public byte[] reservas(ReservationsReportResponse r) {
        Context ctx = baseContext(LocalDateTime.now());
        ctx.setVariable("reservas", r);
        graficosReservas(ctx, r);
        ctx.setVariable("chartReservasTipo",
                r.porTipoHabitacion().isEmpty() ? null : chartService.reservasPorTipo(r.porTipoHabitacion()));
        return render(templateEngine.process("reportes/reservas", ctx));
    }

    public byte[] ocupacion(OccupancyReportResponse r) {
        Context ctx = baseContext(LocalDateTime.now());
        ctx.setVariable("ocupacion", r);
        graficosOcupacion(ctx, r);
        return render(templateEngine.process("reportes/ocupacion", ctx));
    }

    public byte[] asistencia(AttendanceReportResponse r) {
        Context ctx = baseContext(LocalDateTime.now());
        ctx.setVariable("asistencia", r);
        ctx.setVariable("chartAsistenciaTipo",
                r.totalRegistros() == 0 ? null : chartService.asistenciaPorTipo(r));
        ctx.setVariable("chartIncidencias",
                r.porEmpleado().isEmpty() ? null : chartService.incidenciasPorEmpleado(r.porEmpleado()));
        return render(templateEngine.process("reportes/asistencia", ctx));
    }

    public byte[] nomina(PayrollReportResponse r) {
        Context ctx = baseContext(LocalDateTime.now());
        ctx.setVariable("nomina", r);
        ctx.setVariable("chartNomina",
                r.porPeriodo().isEmpty() ? null : chartService.nominaPorPeriodo(r.porPeriodo()));
        return render(templateEngine.process("reportes/nomina", ctx));
    }

    // ---------------------------------------------------------------

    private Context baseContext(LocalDateTime generadoEn) {
        Context ctx = new Context(ES_PE);
        ctx.setVariable("logo", LOGO_DATA_URI);
        ctx.setVariable("generadoEn", generadoEn.format(FECHA_HORA));
        ctx.setVariable("generadoPor", usuarioActual());
        return ctx;
    }

    private void graficosIngresos(Context ctx, RevenueReportResponse r) {
        ctx.setVariable("chartMetodoPago",
                r.porMetodoPago().isEmpty() ? null : chartService.metodoPago(r.porMetodoPago()));
        ctx.setVariable("chartFuente",
                r.porFuente().isEmpty() ? null : chartService.ingresosPorFuente(r.porFuente()));
    }

    private void graficosReservas(Context ctx, ReservationsReportResponse r) {
        ctx.setVariable("chartReservasEstado",
                r.porEstado().isEmpty() ? null : chartService.reservasPorEstado(r.porEstado()));
        ctx.setVariable("chartCanal",
                r.porCanal().isEmpty() ? null : chartService.ingresosPorCanal(r.porCanal()));
    }

    private void graficosOcupacion(Context ctx, OccupancyReportResponse r) {
        ctx.setVariable("chartOcupacionDiaria", chartService.ocupacionDiaria(r.serie()));
        ctx.setVariable("chartOcupacionPct",
                r.porTipoHabitacion().isEmpty() ? null : chartService.ocupacionPctPorTipo(r.porTipoHabitacion()));
        ctx.setVariable("chartTarifas",
                r.porTipoHabitacion().isEmpty() ? null : chartService.tarifasPorTipo(r.porTipoHabitacion()));
    }

    // ---------------------------------------------------------------

    private String usuarioActual() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) return "—";

            // Resuelve la entidad Usuario según el tipo de principal: el login
            // por cookie pone un UserPrincipal (solo userId/correo), mientras
            // que otros flujos usan CustomUserDetails con la entidad completa.
            Usuario u = null;
            if (auth.getPrincipal() instanceof CustomUserDetails cud) {
                u = cud.getUsuario();
            } else if (auth.getPrincipal() instanceof UserPrincipal up && up.userId() != null) {
                u = usuarioRepository.findById(up.userId()).orElse(null);
            }
            if (u == null) return "—";

            String nombre = Stream.of(u.getNombre(), u.getApellidoPaterno(), u.getApellidoMaterno())
                    .filter(Objects::nonNull)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.joining(" "));
            return nombre.isBlank() ? "—" : nombre;
        } catch (Exception e) {
            return "—";
        }
    }

    private byte[] render(String html) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Error generando el PDF del reporte", e);
        }
    }

    private static String cargarLogo() {
        try (InputStream in = new ClassPathResource("reportes/logo.png").getInputStream()) {
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(in.readAllBytes());
        } catch (IOException e) {
            // Sin logo el reporte sigue siendo válido; se omite la imagen.
            return null;
        }
    }
}
