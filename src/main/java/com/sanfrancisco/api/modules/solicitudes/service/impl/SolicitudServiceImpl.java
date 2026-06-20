package com.sanfrancisco.api.modules.solicitudes.service.impl;

import com.sanfrancisco.api.config.security.Permissions;
import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.enums.EstadoUsuario;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import com.sanfrancisco.api.modules.seguridad.security.UserPrincipal;
import com.sanfrancisco.api.modules.solicitudes.dto.request.AsignarResponsableRequest;
import com.sanfrancisco.api.modules.solicitudes.dto.request.CambiarEstadoSolicitudRequest;
import com.sanfrancisco.api.modules.solicitudes.dto.request.CreateSolicitudRequest;
import com.sanfrancisco.api.modules.solicitudes.dto.request.RegistrarSeguimientoRequest;
import com.sanfrancisco.api.modules.solicitudes.dto.request.SolicitudFilterRequest;
import com.sanfrancisco.api.modules.solicitudes.dto.request.UpdateSolicitudRequest;
import com.sanfrancisco.api.modules.solicitudes.dto.response.SeguimientoSolicitudResponse;
import com.sanfrancisco.api.modules.solicitudes.dto.response.SolicitudReporteResponse;
import com.sanfrancisco.api.modules.solicitudes.dto.response.SolicitudResponse;
import com.sanfrancisco.api.modules.solicitudes.entity.SeguimientoSolicitud;
import com.sanfrancisco.api.modules.solicitudes.entity.Solicitud;
import com.sanfrancisco.api.modules.solicitudes.enums.AccionSeguimiento;
import com.sanfrancisco.api.modules.solicitudes.enums.EstadoSolicitud;
import com.sanfrancisco.api.modules.solicitudes.enums.TipoSolicitud;
import com.sanfrancisco.api.modules.notificaciones.service.interfaces.NotificationService;
import com.sanfrancisco.api.modules.solicitudes.mapper.SeguimientoSolicitudMapper;
import com.sanfrancisco.api.modules.solicitudes.mapper.SolicitudMapper;
import com.sanfrancisco.api.modules.solicitudes.repository.SeguimientoSolicitudRepository;
import com.sanfrancisco.api.modules.solicitudes.repository.SolicitudRepository;
import com.sanfrancisco.api.modules.solicitudes.service.interfaces.SolicitudService;
import com.sanfrancisco.api.modules.solicitudes.specification.SolicitudSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class SolicitudServiceImpl implements SolicitudService {

    private static final Logger log = LoggerFactory.getLogger(SolicitudServiceImpl.class);

    /** Transiciones de estado permitidas. CERRADA es terminal. */
    private static final Map<EstadoSolicitud, Set<EstadoSolicitud>> TRANSICIONES =
            new EnumMap<>(EstadoSolicitud.class);

    static {
        TRANSICIONES.put(EstadoSolicitud.REGISTRADA,
                Set.of(EstadoSolicitud.EN_EVALUACION, EstadoSolicitud.CERRADA));
        TRANSICIONES.put(EstadoSolicitud.EN_EVALUACION,
                Set.of(EstadoSolicitud.ATENDIDA, EstadoSolicitud.APROBADA,
                        EstadoSolicitud.RECHAZADA, EstadoSolicitud.CERRADA));
        TRANSICIONES.put(EstadoSolicitud.ATENDIDA, Set.of(EstadoSolicitud.CERRADA));
        TRANSICIONES.put(EstadoSolicitud.APROBADA, Set.of(EstadoSolicitud.CERRADA));
        TRANSICIONES.put(EstadoSolicitud.RECHAZADA, Set.of(EstadoSolicitud.CERRADA));
        TRANSICIONES.put(EstadoSolicitud.CERRADA, Set.of());
    }

    private final SolicitudRepository solicitudRepository;
    private final SeguimientoSolicitudRepository seguimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final SolicitudMapper solicitudMapper;
    private final SeguimientoSolicitudMapper seguimientoMapper;
    private final NotificationService notificationService;

    public SolicitudServiceImpl(SolicitudRepository solicitudRepository,
                                SeguimientoSolicitudRepository seguimientoRepository,
                                UsuarioRepository usuarioRepository,
                                SolicitudMapper solicitudMapper,
                                SeguimientoSolicitudMapper seguimientoMapper,
                                NotificationService notificationService) {
        this.solicitudRepository = solicitudRepository;
        this.seguimientoRepository = seguimientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.solicitudMapper = solicitudMapper;
        this.seguimientoMapper = seguimientoMapper;
        this.notificationService = notificationService;
    }

    // =========================================================================
    // CREAR
    // =========================================================================
    @Override
    public SolicitudResponse create(CreateSolicitudRequest request) {
        Usuario solicitante = getCurrentUsuario();

        // Coherencia para solicitudes de ACCESO
        if (request.tipoSolicitud() == TipoSolicitud.ACCESO
                && (request.rolSolicitado() == null || request.rolSolicitado().isBlank())
                && request.tipoAcceso() == null) {
            throw new BusinessException(
                    "Una solicitud de ACCESO debe indicar el rol solicitado o el tipo de acceso.");
        }

        String codigo = generarCodigo(request.tipoSolicitud());
        Solicitud solicitud = solicitudMapper.toEntity(request, solicitante, codigo);

        // Primer seguimiento: CREACION
        solicitud.addSeguimiento(buildSeguimiento(
                AccionSeguimiento.CREACION, null, EstadoSolicitud.REGISTRADA,
                "Solicitud registrada", solicitante));

        Solicitud saved = solicitudRepository.save(solicitud);
        log.info("Solicitud creada {} por usuarioId={}", saved.getCodigoSolicitud(), solicitante.getUsuarioId());
        return solicitudMapper.toResponse(saved);
    }

    // =========================================================================
    // ACTUALIZAR (solo el autor, solo en estado REGISTRADA)
    // =========================================================================
    @Override
    public SolicitudResponse update(Integer solicitudId, UpdateSolicitudRequest request) {
        Solicitud solicitud = obtenerOFallar(solicitudId);
        UserPrincipal principal = getCurrentPrincipal();

        if (!solicitud.getSolicitante().getUsuarioId().equals(principal.userId())) {
            throw new BusinessException("Solo el autor puede editar la solicitud.");
        }
        if (solicitud.getEstado() != EstadoSolicitud.REGISTRADA) {
            throw new BusinessException(
                    "La solicitud solo puede editarse mientras está en estado REGISTRADA.");
        }

        if (request.asunto() != null && !request.asunto().isBlank()) solicitud.setAsunto(request.asunto());
        if (request.descripcion() != null && !request.descripcion().isBlank()) solicitud.setDescripcion(request.descripcion());
        if (request.prioridad() != null) solicitud.setPrioridad(request.prioridad());
        if (request.moduloReferido() != null) solicitud.setModuloReferido(request.moduloReferido());

        return solicitudMapper.toResponse(solicitudRepository.save(solicitud));
    }

    // =========================================================================
    // CONSULTAR
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public SolicitudResponse findById(Integer solicitudId) {
        Solicitud solicitud = obtenerOFallar(solicitudId);
        verificarAcceso(solicitud);
        return solicitudMapper.toResponse(solicitud);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SolicitudResponse> search(SolicitudFilterRequest filter, Pageable pageable) {
        Specification<Solicitud> spec = SolicitudSpecification.build(filter);

        // Sin permiso read-all → el usuario solo ve sus propias solicitudes
        if (!tienePermiso(Permissions.SOLICITUD_READ_ALL)) {
            spec = spec.and(SolicitudSpecification.ownedBy(getCurrentPrincipal().userId()));
        }

        return solicitudRepository.findAll(spec, pageable).map(solicitudMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeguimientoSolicitudResponse> getSeguimientos(Integer solicitudId) {
        Solicitud solicitud = obtenerOFallar(solicitudId);
        verificarAcceso(solicitud);
        return seguimientoRepository
                .findBySolicitud_SolicitudIdOrderByFechaAccionAsc(solicitudId)
                .stream()
                .map(seguimientoMapper::toResponse)
                .toList();
    }

    // =========================================================================
    // ASIGNAR RESPONSABLE
    // =========================================================================
    @Override
    public SolicitudResponse asignarResponsable(Integer solicitudId, AsignarResponsableRequest request) {
        Solicitud solicitud = obtenerOFallar(solicitudId);

        if (solicitud.getEstado() == EstadoSolicitud.CERRADA) {
            throw new BusinessException("No se puede asignar responsable a una solicitud CERRADA.");
        }

        Usuario responsable = usuarioRepository.findById(request.responsableId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario responsable no encontrado: " + request.responsableId()));
        if (responsable.getEstado() != EstadoUsuario.ACTIVO) {
            throw new BusinessException("El responsable asignado no está activo.");
        }

        solicitud.setResponsable(responsable);

        EstadoSolicitud anterior = solicitud.getEstado();
        // Al asignar, si seguía REGISTRADA pasa automáticamente a EN_EVALUACION
        if (anterior == EstadoSolicitud.REGISTRADA) {
            solicitud.setEstado(EstadoSolicitud.EN_EVALUACION);
        }

        solicitud.addSeguimiento(buildSeguimiento(
                AccionSeguimiento.ASIGNACION, anterior, solicitud.getEstado(),
                request.observacion() != null ? request.observacion()
                        : "Asignado a " + SeguimientoSolicitudMapper.buildNombre(responsable),
                getCurrentUsuario()));

        return solicitudMapper.toResponse(solicitudRepository.save(solicitud));
    }

    // =========================================================================
    // CAMBIAR ESTADO
    // =========================================================================
    @Override
    public SolicitudResponse cambiarEstado(Integer solicitudId, CambiarEstadoSolicitudRequest request) {
        Solicitud solicitud = obtenerOFallar(solicitudId);
        EstadoSolicitud actual = solicitud.getEstado();
        EstadoSolicitud nuevo = request.nuevoEstado();

        // Regla RBAC: un operador con change-status pero sin read-all (RECEPCION)
        // solo puede operar solicitudes de tipo INFORMACION.
        if (tienePermiso(Permissions.SOLICITUD_CHANGE_STATUS)
                && !tienePermiso(Permissions.SOLICITUD_READ_ALL)
                && solicitud.getTipoSolicitud() != TipoSolicitud.INFORMACION) {
            throw new BusinessException(
                    "No tiene permiso para cambiar el estado de solicitudes de tipo ACCESO.");
        }

        if (actual == nuevo) {
            throw new BusinessException("La solicitud ya se encuentra en estado " + nuevo);
        }
        Set<EstadoSolicitud> permitidos = TRANSICIONES.getOrDefault(actual, Set.of());
        if (!permitidos.contains(nuevo)) {
            throw new BusinessException("Transición de estado no permitida: " + actual + " -> " + nuevo);
        }

        // Coherencia estado ↔ tipo
        if (nuevo == EstadoSolicitud.ATENDIDA && solicitud.getTipoSolicitud() != TipoSolicitud.INFORMACION) {
            throw new BusinessException("El estado ATENDIDA solo aplica a solicitudes de tipo INFORMACION.");
        }
        if ((nuevo == EstadoSolicitud.APROBADA || nuevo == EstadoSolicitud.RECHAZADA)
                && solicitud.getTipoSolicitud() != TipoSolicitud.ACCESO) {
            throw new BusinessException(
                    "Los estados APROBADA/RECHAZADA solo aplican a solicitudes de tipo ACCESO.");
        }
        if (nuevo == EstadoSolicitud.RECHAZADA
                && (request.observacion() == null || request.observacion().isBlank())) {
            throw new BusinessException("Debe indicar el motivo del rechazo en la observación.");
        }
        if (nuevo == EstadoSolicitud.CERRADA && solicitud.getResponsable() == null) {
            throw new BusinessException("No se puede cerrar una solicitud sin responsable asignado.");
        }

        solicitud.setEstado(nuevo);
        if (request.observacion() != null && !request.observacion().isBlank()) {
            solicitud.setObservaciones(request.observacion());
        }
        if (nuevo == EstadoSolicitud.CERRADA) {
            solicitud.setFechaCierre(LocalDateTime.now());
        }

        solicitud.addSeguimiento(buildSeguimiento(
                deriveAccion(nuevo), actual, nuevo, request.observacion(), getCurrentUsuario()));

        log.info("Solicitud {} cambió de {} a {}", solicitud.getCodigoSolicitud(), actual, nuevo);
        Solicitud saved = solicitudRepository.save(solicitud);

        // Notificación al solicitante (fire-and-forget; fallo no interrumpe la operación)
        try {
            Usuario solicitante = saved.getSolicitante();
            if (solicitante.getCorreo() != null && !solicitante.getCorreo().isBlank()) {
                notificationService.sendSolicitudStatusChanged(
                        solicitante.getCorreo(),
                        SeguimientoSolicitudMapper.buildNombre(solicitante),
                        saved.getCodigoSolicitud(),
                        saved.getAsunto(),
                        actual.name(),
                        nuevo.name(),
                        request.observacion()
                );
            }
        } catch (Exception e) {
            log.warn("No se pudo enviar notificación de estado para solicitud {}: {}",
                    saved.getCodigoSolicitud(), e.getMessage());
        }

        return solicitudMapper.toResponse(saved);
    }

    // =========================================================================
    // REGISTRAR OBSERVACIÓN (sin cambio de estado)
    // =========================================================================
    @Override
    public SeguimientoSolicitudResponse registrarObservacion(Integer solicitudId,
                                                             RegistrarSeguimientoRequest request) {
        Solicitud solicitud = obtenerOFallar(solicitudId);
        if (solicitud.getEstado() == EstadoSolicitud.CERRADA) {
            throw new BusinessException("No se pueden añadir observaciones a una solicitud CERRADA.");
        }

        SeguimientoSolicitud seguimiento = buildSeguimiento(
                AccionSeguimiento.OBSERVACION, solicitud.getEstado(), solicitud.getEstado(),
                request.observacion(), getCurrentUsuario());
        solicitud.addSeguimiento(seguimiento);
        solicitudRepository.save(solicitud);

        // El seguimiento recién persistido es el último de la lista
        List<SeguimientoSolicitud> lista = solicitud.getSeguimientos();
        return seguimientoMapper.toResponse(lista.get(lista.size() - 1));
    }

    // =========================================================================
    // REPORTE CONSOLIDADO
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public SolicitudReporteResponse generarReporte() {
        long total = solicitudRepository.count();

        Map<EstadoSolicitud, Long> porEstado = new EnumMap<>(EstadoSolicitud.class);
        for (EstadoSolicitud e : EstadoSolicitud.values()) {
            porEstado.put(e, solicitudRepository.countByEstado(e));
        }

        Map<TipoSolicitud, Long> porTipo = new EnumMap<>(TipoSolicitud.class);
        for (TipoSolicitud t : TipoSolicitud.values()) {
            porTipo.put(t, solicitudRepository.countByTipoSolicitud(t));
        }

        long cerradas = porEstado.getOrDefault(EstadoSolicitud.CERRADA, 0L);
        long pendientes = total - cerradas;

        return new SolicitudReporteResponse(total, porEstado, porTipo, pendientes, cerradas);
    }

    // =========================================================================
    // ELIMINAR (baja física controlada; solo ADMIN vía permiso)
    // =========================================================================
    @Override
    public void deleteById(Integer solicitudId) {
        Solicitud solicitud = obtenerOFallar(solicitudId);
        solicitudRepository.delete(solicitud);
        log.info("Solicitud {} eliminada", solicitud.getCodigoSolicitud());
    }

    // =========================================================================
    // Helpers
    // =========================================================================
    private Solicitud obtenerOFallar(Integer solicitudId) {
        return solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada: " + solicitudId));
    }

    /**
     * Verifica que el usuario actual pueda ver la solicitud: o tiene read-all,
     * o es el autor. Se lanza 404 (no 403) para no revelar la existencia del recurso.
     */
    private void verificarAcceso(Solicitud solicitud) {
        if (tienePermiso(Permissions.SOLICITUD_READ_ALL)) return;
        Integer currentUserId = getCurrentPrincipal().userId();
        if (!solicitud.getSolicitante().getUsuarioId().equals(currentUserId)) {
            throw new ResourceNotFoundException("Solicitud no encontrada: " + solicitud.getSolicitudId());
        }
    }

    private SeguimientoSolicitud buildSeguimiento(AccionSeguimiento accion,
                                                  EstadoSolicitud anterior,
                                                  EstadoSolicitud nuevo,
                                                  String observacion,
                                                  Usuario responsable) {
        return SeguimientoSolicitud.builder()
                .fechaAccion(LocalDateTime.now())
                .accion(accion)
                .estadoAnterior(anterior)
                .estadoNuevo(nuevo)
                .observacion(observacion)
                .responsable(responsable)
                .build();
    }

    private AccionSeguimiento deriveAccion(EstadoSolicitud nuevo) {
        return switch (nuevo) {
            case APROBADA -> AccionSeguimiento.APROBACION;
            case RECHAZADA -> AccionSeguimiento.RECHAZO;
            case CERRADA -> AccionSeguimiento.CIERRE;
            default -> AccionSeguimiento.CAMBIO_ESTADO;
        };
    }

    /** Genera un código tipo SOL-2026-001 (INFORMACION) o ACC-2026-001 (ACCESO). */
    private String generarCodigo(TipoSolicitud tipo) {
        String prefijo = tipo == TipoSolicitud.ACCESO ? "ACC" : "SOL";
        int anio = LocalDate.now().getYear();
        LocalDateTime inicioAnio = LocalDate.of(anio, 1, 1).atStartOfDay();
        LocalDateTime finAnio = LocalDate.of(anio, 12, 31).atTime(LocalTime.MAX);

        long correlativo = solicitudRepository
                .countByTipoSolicitudAndFechaRegistroBetween(tipo, inicioAnio, finAnio) + 1;

        String codigo = String.format("%s-%d-%03d", prefijo, anio, correlativo);
        // Red de seguridad ante colisiones por concurrencia
        while (solicitudRepository.existsByCodigoSolicitud(codigo)) {
            correlativo++;
            codigo = String.format("%s-%d-%03d", prefijo, anio, correlativo);
        }
        return codigo;
    }

    private Usuario getCurrentUsuario() {
        Integer userId = getCurrentPrincipal().userId();
        return usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userId));
    }

    private UserPrincipal getCurrentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessException("No autenticado.");
        }
        return principal;
    }

    private boolean tienePermiso(String permiso) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if (permiso.equals(ga.getAuthority())) return true;
        }
        return false;
    }
}
