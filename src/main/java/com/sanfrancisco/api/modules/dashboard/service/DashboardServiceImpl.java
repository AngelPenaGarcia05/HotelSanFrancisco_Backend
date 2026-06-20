package com.sanfrancisco.api.modules.dashboard.service;

import com.sanfrancisco.api.config.security.Permissions;
import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.auditoria.dto.request.AuditoriaFilterRequest;
import com.sanfrancisco.api.modules.solicitudes.enums.EstadoSolicitud;
import com.sanfrancisco.api.modules.solicitudes.repository.SolicitudRepository;
import com.sanfrancisco.api.modules.auditoria.enums.ResultadoAuditoria;
import com.sanfrancisco.api.modules.auditoria.repository.RegistroAuditoriaRepository;
import com.sanfrancisco.api.modules.auditoria.specification.RegistroAuditoriaSpecification;
import com.sanfrancisco.api.modules.dashboard.dto.response.DashboardResponse;
import com.sanfrancisco.api.modules.inventario.repository.ProductoRepository;
import com.sanfrancisco.api.modules.operaciones.enums.EstadoIncidencia;
import com.sanfrancisco.api.modules.operaciones.repository.IncidenciaRepository;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoHabitacion;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import com.sanfrancisco.api.modules.recepcion.repository.HabitacionRepository;
import com.sanfrancisco.api.modules.recepcion.repository.ReservaRepository;
import com.sanfrancisco.api.modules.reportes.dto.request.ReportRangeRequest;
import com.sanfrancisco.api.modules.reportes.dto.response.ReservationsReportResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.RevenueReportResponse;
import com.sanfrancisco.api.modules.reportes.service.interfaces.ReportService;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.enums.EstadoUsuario;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import com.sanfrancisco.api.modules.seguridad.security.UserPrincipal;
import com.sanfrancisco.api.modules.ventas.entity.Venta;
import com.sanfrancisco.api.modules.ventas.enums.EstadoVenta;
import com.sanfrancisco.api.modules.ventas.repository.VentaRepository;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);
    private static final String ROL_CLIENTE = "CLIENTE";

    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;
    private final HabitacionRepository habitacionRepository;
    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final RegistroAuditoriaRepository registroAuditoriaRepository;
    private final ReportService reportService;
    private final SolicitudRepository solicitudRepository;

    public DashboardServiceImpl(UsuarioRepository usuarioRepository,
                                ReservaRepository reservaRepository,
                                HabitacionRepository habitacionRepository,
                                VentaRepository ventaRepository,
                                ProductoRepository productoRepository,
                                IncidenciaRepository incidenciaRepository,
                                RegistroAuditoriaRepository registroAuditoriaRepository,
                                ReportService reportService,
                                SolicitudRepository solicitudRepository) {
        this.usuarioRepository = usuarioRepository;
        this.reservaRepository = reservaRepository;
        this.habitacionRepository = habitacionRepository;
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.incidenciaRepository = incidenciaRepository;
        this.registroAuditoriaRepository = registroAuditoriaRepository;
        this.reportService = reportService;
        this.solicitudRepository = solicitudRepository;
    }

    @Override
    public DashboardResponse build() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BadCredentialsException("No autenticado");
        }

        Set<String> permisos = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> !a.startsWith("ROLE_"))
                .collect(Collectors.toSet());

        Usuario usuario = usuarioRepository.findById(principal.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + principal.userId()));

        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate inicioMesAnterior = inicioMes.minusMonths(1);
        LocalDate finMesAnterior = inicioMes.minusDays(1);

        return new DashboardResponse(
                LocalDateTime.now(),
                buildUsuarioInfo(usuario, permisos),
                permisos.contains(Permissions.RESERVA_READ) ? buildReservas(inicioMes, hoy) : null,
                permisos.contains(Permissions.HABITACION_READ) ? buildOcupacion() : null,
                permisos.contains(Permissions.PAGO_READ) ? buildIngresos(inicioMes, hoy, inicioMesAnterior, finMesAnterior) : null,
                permisos.contains(Permissions.VENTA_READ) ? buildVentas(inicioMes) : null,
                permisos.contains(Permissions.PRODUCTO_READ) ? buildInventario() : null,
                permisos.contains(Permissions.INCIDENCIA_READ) ? buildIncidencias() : null,
                permisos.contains(Permissions.USUARIO_READ) ? buildUsuarios() : null,
                permisos.contains(Permissions.AUDITORIA_READ) ? buildAuditoria(hoy) : null,
                permisos.contains(Permissions.SOLICITUD_READ) ? buildSolicitudes() : null
        );
    }

    // =====================================================================
    // Tarjetas
    // =====================================================================

    private DashboardResponse.UsuarioInfo buildUsuarioInfo(Usuario usuario, Set<String> permisos) {
        String nombreCompleto = usuario.getNombre() + " " + usuario.getApellidoPaterno()
                + (usuario.getApellidoMaterno() != null && !usuario.getApellidoMaterno().isBlank()
                ? " " + usuario.getApellidoMaterno() : "");
        String rol = usuario.getRol() != null ? usuario.getRol().getNombre() : null;
        return new DashboardResponse.UsuarioInfo(
                usuario.getUsuarioId(), nombreCompleto, usuario.getCorreo(), rol,
                permisos.stream().sorted().toList());
    }

    private DashboardResponse.ReservasCard buildReservas(LocalDate inicioMes, LocalDate hoy) {
        long activas = reservaRepository.findByEstado(EstadoReserva.CONFIRMADA).size()
                + reservaRepository.findByEstado(EstadoReserva.CHECK_IN).size();

        long pendientesPago = reservaRepository.findAll().stream()
                .filter(r -> r.getEstado() != EstadoReserva.CANCELADA && r.getEstado() != EstadoReserva.NO_SHOW)
                .filter(r -> r.getAdelanto().compareTo(r.getMontoTotal()) < 0)
                .count();

        ReservationsReportResponse reservasMes = reportService.buildReservationsReport(
                new ReportRangeRequest("CUSTOM", "DAY", inicioMes, hoy));

        return new DashboardResponse.ReservasCard(
                activas, pendientesPago, reservasMes.totalReservas(), reservasMes.totalCanceladas());
    }

    private DashboardResponse.OcupacionCard buildOcupacion() {
        long totales = habitacionRepository.count();
        long ocupadas = habitacionRepository.findByEstado(EstadoHabitacion.OCUPADA).size();
        long disponibles = habitacionRepository.findByEstado(EstadoHabitacion.DISPONIBLE).size();
        long limpieza = habitacionRepository.findByEstado(EstadoHabitacion.LIMPIEZA).size();
        long mantenimiento = habitacionRepository.findByEstado(EstadoHabitacion.MANTENIMIENTO).size();

        BigDecimal pctOcupacion = totales == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(ocupadas).multiply(CIEN)
                .divide(BigDecimal.valueOf(totales), 1, RoundingMode.HALF_UP);

        return new DashboardResponse.OcupacionCard(
                totales, ocupadas, disponibles, limpieza, mantenimiento, pctOcupacion);
    }

    private DashboardResponse.IngresosCard buildIngresos(LocalDate inicioMes, LocalDate hoy,
                                                         LocalDate inicioMesAnterior, LocalDate finMesAnterior) {
        RevenueReportResponse actual = reportService.buildRevenueReport(
                new ReportRangeRequest("CUSTOM", "DAY", inicioMes, hoy));
        RevenueReportResponse anterior = reportService.buildRevenueReport(
                new ReportRangeRequest("CUSTOM", "DAY", inicioMesAnterior, finMesAnterior));

        return new DashboardResponse.IngresosCard(
                actual.totalIngresos(),
                anterior.totalIngresos(),
                variacionPorcentual(anterior.totalIngresos(), actual.totalIngresos()));
    }

    private DashboardResponse.VentasCard buildVentas(LocalDate inicioMes) {
        List<Venta> ventasMes = ventaRepository.findByFechaVentaBetween(
                inicioMes.atStartOfDay(), LocalDateTime.now());

        long ventasMesCount = ventasMes.stream()
                .filter(v -> v.getEstado() != EstadoVenta.ANULADA)
                .count();

        BigDecimal montoVentasMes = ventasMes.stream()
                .filter(v -> v.getEstado() == EstadoVenta.COMPLETADA)
                .map(Venta::getMontoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendientes = ventaRepository.findByEstado(EstadoVenta.PENDIENTE).size();

        return new DashboardResponse.VentasCard(ventasMesCount, montoVentasMes, pendientes);
    }

    private DashboardResponse.InventarioCard buildInventario() {
        long activos = productoRepository.findByEstado(EstadoActivo.ACTIVO).size();
        long bajoStock = productoRepository.findProductosBajoStock().size();
        return new DashboardResponse.InventarioCard(activos, bajoStock);
    }

    private DashboardResponse.IncidenciasCard buildIncidencias() {
        long abiertas = incidenciaRepository.findByEstado(EstadoIncidencia.ABIERTA).size();
        long enProceso = incidenciaRepository.findByEstado(EstadoIncidencia.EN_PROCESO).size();
        long total = incidenciaRepository.count();
        return new DashboardResponse.IncidenciasCard(abiertas, enProceso, total);
    }

    private DashboardResponse.UsuariosCard buildUsuarios() {
        List<Usuario> todos = usuarioRepository.findAll();
        long total = todos.size();
        long activos = todos.stream().filter(u -> u.getEstado() == EstadoUsuario.ACTIVO).count();
        long clientes = todos.stream()
                .filter(u -> u.getRol() != null && ROL_CLIENTE.equalsIgnoreCase(u.getRol().getNombre()))
                .count();
        long empleados = total - clientes;
        return new DashboardResponse.UsuariosCard(total, activos, empleados, clientes);
    }

    private DashboardResponse.AuditoriaCard buildAuditoria(LocalDate hoy) {
        long accionesHoy = registroAuditoriaRepository.count(
                RegistroAuditoriaSpecification.build(
                        new AuditoriaFilterRequest(null, null, null, null, null, hoy, hoy)));
        long erroresHoy = registroAuditoriaRepository.count(
                RegistroAuditoriaSpecification.build(
                        new AuditoriaFilterRequest(null, null, null, null, ResultadoAuditoria.ERROR, hoy, hoy)));
        return new DashboardResponse.AuditoriaCard(accionesHoy, erroresHoy);
    }

    private DashboardResponse.SolicitudesCard buildSolicitudes() {
        long total = solicitudRepository.count();
        long pendientes = solicitudRepository.countByEstado(EstadoSolicitud.REGISTRADA);
        long enEvaluacion = solicitudRepository.countByEstado(EstadoSolicitud.EN_EVALUACION);
        long cerradas = solicitudRepository.countByEstado(EstadoSolicitud.CERRADA);
        return new DashboardResponse.SolicitudesCard(total, pendientes, enEvaluacion, cerradas);
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private BigDecimal variacionPorcentual(BigDecimal anterior, BigDecimal actual) {
        if (anterior == null || anterior.compareTo(BigDecimal.ZERO) == 0) {
            return actual != null && actual.compareTo(BigDecimal.ZERO) > 0 ? CIEN : BigDecimal.ZERO;
        }
        return actual.subtract(anterior).multiply(CIEN).divide(anterior, 1, RoundingMode.HALF_UP);
    }
}
