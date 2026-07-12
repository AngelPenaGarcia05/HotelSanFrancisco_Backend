package com.sanfrancisco.api.modules.reportes.export;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.sanfrancisco.api.modules.reportes.dto.response.ManagementDashboardResponse;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.security.CustomUserDetails;
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

    public PdfReportExporter(TemplateEngine reportesTemplateEngine,
                             ChartImageService chartService) {
        this.templateEngine = reportesTemplateEngine;
        this.chartService = chartService;
    }

    public byte[] gerencial(ManagementDashboardResponse r) {
        Context ctx = new Context(ES_PE);
        // Encabezado común
        ctx.setVariable("logo", LOGO_DATA_URI);
        ctx.setVariable("titulo", "Reporte gerencial — Resumen ejecutivo");
        ctx.setVariable("generadoEn", r.generadoEn().format(FECHA_HORA));
        ctx.setVariable("generadoPor", usuarioActual());
        // Datos
        ctx.setVariable("kpis", r.kpis());
        ctx.setVariable("ingresos", r.ingresos());
        ctx.setVariable("reservas", r.reservas());
        ctx.setVariable("ocupacion", r.ocupacion());
        // Gráficos (data-URI PNG), cada uno junto a su tabla
        ctx.setVariable("chartOcupacionDiaria", chartService.ocupacionDiaria(r.ocupacion().serie()));
        ctx.setVariable("chartMetodoPago",
                r.ingresos().porMetodoPago().isEmpty() ? null
                        : chartService.metodoPago(r.ingresos().porMetodoPago()));
        ctx.setVariable("chartReservasEstado",
                r.reservas().porEstado().isEmpty() ? null
                        : chartService.reservasPorEstado(r.reservas().porEstado()));
        ctx.setVariable("chartOcupacionPct",
                r.ocupacion().porTipoHabitacion().isEmpty() ? null
                        : chartService.ocupacionPctPorTipo(r.ocupacion().porTipoHabitacion()));
        ctx.setVariable("chartTarifas",
                r.ocupacion().porTipoHabitacion().isEmpty() ? null
                        : chartService.tarifasPorTipo(r.ocupacion().porTipoHabitacion()));

        String html = templateEngine.process("reportes/gerencial", ctx);
        return render(html);
    }

    // ---------------------------------------------------------------

    private String usuarioActual() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails cud)) {
                return "—";
            }
            Usuario u = cud.getUsuario();
            String nombre = Stream.of(u.getNombre(), u.getApellidoPaterno(), u.getApellidoMaterno())
                    .filter(Objects::nonNull)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.joining(" "));
            // El rol se toma de las authorities (evita cargar la relación lazy
            // de una entidad detached proveniente del token de autenticación).
            String rol = auth.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .filter(a -> a.startsWith("ROLE_"))
                    .map(a -> a.substring(5))
                    .findFirst()
                    .orElse(null);
            if (nombre.isBlank()) return "—";
            return (rol != null && !rol.isBlank()) ? nombre + " (" + rol + ")" : nombre;
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
