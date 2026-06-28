package com.sanfrancisco.api.modules.recepcion.service.impl;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.recepcion.dto.ReservaMontos;
import com.sanfrancisco.api.modules.recepcion.dto.request.*;
import com.sanfrancisco.api.modules.recepcion.dto.response.CancelacionResponse;
import com.sanfrancisco.api.modules.recepcion.dto.response.HistorialReservaResponse;
import com.sanfrancisco.api.modules.recepcion.dto.response.ReservaResponse;
import com.sanfrancisco.api.modules.recepcion.entity.*;
import com.sanfrancisco.api.modules.notificacionescliente.enums.TipoNotificacionHuesped;
import com.sanfrancisco.api.modules.notificacionescliente.service.interfaces.NotificacionClienteService;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoHabitacion;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import com.sanfrancisco.api.modules.recepcion.enums.ModalidadPago;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReservaHabitacion;
import com.sanfrancisco.api.modules.recepcion.mapper.DetalleHuespedMapper;
import com.sanfrancisco.api.modules.recepcion.mapper.HistorialReservaMapper;
import com.sanfrancisco.api.modules.recepcion.mapper.ReservaHabitacionMapper;
import com.sanfrancisco.api.modules.recepcion.mapper.ReservaMapper;
import com.sanfrancisco.api.modules.recepcion.repository.*;
import com.sanfrancisco.api.modules.recepcion.entity.Estancia;
import com.sanfrancisco.api.modules.recepcion.service.interfaces.DisponibilidadService;
import com.sanfrancisco.api.modules.recepcion.service.interfaces.ReservaService;

import java.util.Collections;
import com.sanfrancisco.api.modules.recepcion.specification.ReservaSpecification;
import com.sanfrancisco.api.modules.recepcion.websocket.ReservaEventPublisher;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import com.sanfrancisco.api.shared.exception.ConflictException;
import com.sanfrancisco.api.shared.exception.ValidationException;
import com.sanfrancisco.api.shared.utils.DateTimeUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservaServiceImpl implements ReservaService {

    private static final BigDecimal IGV = new BigDecimal("0.18");
    private static final BigDecimal PORC_ADELANTO_PARCIAL = new BigDecimal("0.50");
    private static final BigDecimal MAX_DESCUENTO_RATIO = new BigDecimal("0.30");   // tope 30% del subtotal (staff)
    private static final BigDecimal TARIFA_MIN_RATIO = new BigDecimal("0.50");      // tarifa pactada >= 50% del precio base
    private static final BigDecimal TARIFA_MAX_RATIO = new BigDecimal("2.00");      // tarifa pactada <= 200% del precio base
    private static final java.util.Random RNG = new java.util.Random();

    private static final Map<EstadoReserva, Set<EstadoReserva>> TRANSICIONES = new EnumMap<>(EstadoReserva.class);

    static {
        TRANSICIONES.put(EstadoReserva.PENDIENTE, Set.of(EstadoReserva.CONFIRMADA, EstadoReserva.CANCELADA));
        TRANSICIONES.put(EstadoReserva.CONFIRMADA, Set.of(EstadoReserva.CHECK_IN, EstadoReserva.CANCELADA, EstadoReserva.NO_SHOW));
        TRANSICIONES.put(EstadoReserva.CHECK_IN, Set.of(EstadoReserva.CHECK_OUT));
        TRANSICIONES.put(EstadoReserva.CHECK_OUT, Set.of());
        TRANSICIONES.put(EstadoReserva.CANCELADA, Set.of());
        TRANSICIONES.put(EstadoReserva.NO_SHOW, Set.of());
    }

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CanalRepository canalRepository;
    private final HabitacionRepository habitacionRepository;
    private final TipoHabitacionRepository tipoHabitacionRepository;
    private final HuespedRepository huespedRepository;
    private final ReservaHabitacionRepository reservaHabitacionRepository;
    private final DetalleHuespedRepository detalleHuespedRepository;
    private final EstanciaRepository estanciaRepository;
    private final ReservaMapper reservaMapper;
    private final ReservaHabitacionMapper reservaHabitacionMapper;
    private final DetalleHuespedMapper detalleHuespedMapper;
    private final HistorialReservaMapper historialReservaMapper;
    private final HistorialReservaRepository historialReservaRepository;
    private final DisponibilidadService disponibilidadService;
    private final ReservaEventPublisher eventPublisher;
    private final NotificacionClienteService notificacionClienteService;

    public ReservaServiceImpl(ReservaRepository reservaRepository,
                              UsuarioRepository usuarioRepository,
                              CanalRepository canalRepository,
                              HabitacionRepository habitacionRepository,
                              TipoHabitacionRepository tipoHabitacionRepository,
                              HuespedRepository huespedRepository,
                              ReservaHabitacionRepository reservaHabitacionRepository,
                              DetalleHuespedRepository detalleHuespedRepository,
                              EstanciaRepository estanciaRepository,
                              ReservaMapper reservaMapper,
                              ReservaHabitacionMapper reservaHabitacionMapper,
                              DetalleHuespedMapper detalleHuespedMapper,
                              HistorialReservaMapper historialReservaMapper,
                              HistorialReservaRepository historialReservaRepository,
                              DisponibilidadService disponibilidadService,
                              ReservaEventPublisher eventPublisher,
                              NotificacionClienteService notificacionClienteService) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.canalRepository = canalRepository;
        this.habitacionRepository = habitacionRepository;
        this.tipoHabitacionRepository = tipoHabitacionRepository;
        this.huespedRepository = huespedRepository;
        this.reservaHabitacionRepository = reservaHabitacionRepository;
        this.detalleHuespedRepository = detalleHuespedRepository;
        this.estanciaRepository = estanciaRepository;
        this.reservaMapper = reservaMapper;
        this.reservaHabitacionMapper = reservaHabitacionMapper;
        this.detalleHuespedMapper = detalleHuespedMapper;
        this.historialReservaMapper = historialReservaMapper;
        this.historialReservaRepository = historialReservaRepository;
        this.disponibilidadService = disponibilidadService;
        this.eventPublisher = eventPublisher;
        this.notificacionClienteService = notificacionClienteService;
    }

    @Override
    public ReservaResponse create(CreateReservaRequest request) {
        // Alta por staff (RECEPCIONISTA/ADMIN): puede pactar tarifa y aplicar descuento (con tope).
        return crearInterno(request, false);
    }

    private ReservaResponse crearInterno(CreateReservaRequest request, boolean esCliente) {
        validarFechas(request.fechaInicio(), request.fechaFin());
        validarUnSoloPrincipal(request.huespedes());

        // El código lo genera el backend salvo que un llamador interno (p.ej. canal online)
        // provea uno explícito, en cuyo caso se valida unicidad. El wizard manda null.
        String codReserva = resolverCodReserva(request.codReserva());

        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + request.usuarioId()));

        Canal canal = null;
        if (request.canalId() != null) {
            canal = canalRepository.findById(request.canalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Canal no encontrado: " + request.canalId()));
        }

        long noches = ChronoUnit.DAYS.between(request.fechaInicio(), request.fechaFin());

        // Cargar y validar habitaciones antes de persistir la reserva
        List<ReservaHabitacionRequest> habRequests = request.habitaciones();
        List<Habitacion> habitaciones = cargarYValidarHabitaciones(habRequests);
        List<TipoHabitacion> tipos = cargarTipos(habRequests);

        // Validar disponibilidad (sin excluir ninguna reserva porque es alta nueva)
        disponibilidadService.validarDisponibilidad(habRequests, request.fechaInicio(), request.fechaFin(), null);

        // Detectar posible reserva duplicada del huésped principal (a menos que el cliente fuerce el alta)
        boolean forzar = Boolean.TRUE.equals(request.forzar());
        if (!forzar) {
            detectarDuplicado(request.huespedes(), request.fechaInicio(), request.fechaFin());
        }

        // Normalizar tarifas (cliente => precio base; staff => validar rango) y recalcular montos server-side
        List<ReservaHabitacionRequest> habNormalizadas = normalizarTarifas(habRequests, tipos, esCliente);
        BigDecimal subtotal = calcularSubtotal(habNormalizadas, tipos, noches);
        BigDecimal descuento = normalizarDescuento(request.descuento(), subtotal, esCliente);
        ReservaMontos montos = calcularMontos(codReserva, subtotal, descuento, request.modalidadPago());

        Reserva entity = reservaMapper.toEntity(request, usuario, canal, montos);
        Reserva saved = reservaRepository.save(entity);

        List<ReservaHabitacion> reservaHabitaciones = persistirHabitaciones(habNormalizadas, saved, habitaciones, tipos, noches);
        List<DetalleHuesped> detalleHuespedes = persistirHuespedes(request.huespedes(), saved);

        registrarHistorial(saved, null, EstadoReserva.PENDIENTE, "Alta de reserva");
        eventPublisher.publishCreated(saved);

        notificacionClienteService.registrar(
                saved.getUsuario().getUsuarioId(),
                TipoNotificacionHuesped.CONFIRMACION,
                "Reserva registrada",
                "Tu reserva " + saved.getCodReserva() + " ha sido registrada para el "
                        + saved.getFechaInicio() + " al " + saved.getFechaFin() + ".",
                saved.getReservaId());

        return reservaMapper.toResponse(saved, reservaHabitaciones, detalleHuespedes);
    }

    @Override
    public ReservaResponse createParaCliente(CreateReservaRequest request, Integer usuarioId) {
        // Seguridad: el usuario (y el huésped) se derivan del JWT, nunca del body.
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + usuarioId));

        List<HuespedReservaRequest> huespedes = request.huespedes();
        if (huespedes == null || huespedes.isEmpty()) {
            Huesped huesped = obtenerOCrearHuespedDeUsuario(usuario);
            huespedes = List.of(new HuespedReservaRequest(huesped.getHuespedId(), true));
        }

        CreateReservaRequest fullRequest = new CreateReservaRequest(
                null,                       // codReserva lo genera el backend
                request.fechaInicio(),
                request.fechaFin(),
                request.nroAdultos(),
                request.nroNinos(),
                BigDecimal.ZERO,            // descuento: el cliente nunca aplica descuento
                null,                       // adelanto: lo deriva el backend de la modalidad
                null,                       // impuesto: lo recalcula el backend
                request.modalidadPago(),
                request.observaciones(),
                usuarioId,
                request.canalId(),
                request.habitaciones(),
                huespedes,
                request.forzar()
        );

        return crearInterno(fullRequest, true);
    }

    /**
     * Devuelve el huésped asociado al usuario autenticado; si no existe, lo crea a partir
     * de los datos del usuario y lo enlaza por usuario_id.
     */
    private Huesped obtenerOCrearHuespedDeUsuario(Usuario usuario) {
        return huespedRepository.findByUsuarioUsuarioId(usuario.getUsuarioId())
                .orElseGet(() -> huespedRepository.save(
                        Huesped.builder()
                                .nombre(usuario.getNombre())
                                .apellidoPaterno(usuario.getApellidoPaterno())
                                .apellidoMaterno(usuario.getApellidoMaterno())
                                .numeroDocumento(usuario.getNumeroDocumento())
                                .correo(usuario.getCorreo())
                                .telefono(usuario.getTelefono())
                                .estado(EstadoActivo.ACTIVO)
                                .usuario(usuario)
                                .build()
                ));
    }

    @Override
    public ReservaResponse update(Integer reservaId, UpdateReservaRequest request) {
        Reserva reserva = obtenerOFallar(reservaId);

        if (esEstadoTerminal(reserva.getEstado())) {
            throw new BusinessException("No se puede modificar una reserva en estado " + reserva.getEstado());
        }

        Canal canal = null;
        if (request.canalId() != null) {
            canal = canalRepository.findById(request.canalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Canal no encontrado: " + request.canalId()));
        }

        LocalDate fechaInicio = request.fechaInicio() != null ? request.fechaInicio() : reserva.getFechaInicio();
        LocalDate fechaFin    = request.fechaFin()    != null ? request.fechaFin()    : reserva.getFechaFin();
        validarFechas(fechaInicio, fechaFin);

        boolean fechasCambiaron = !fechaInicio.equals(reserva.getFechaInicio())
                               || !fechaFin.equals(reserva.getFechaFin());

        BigDecimal subtotalCalculado = null;
        List<ReservaHabitacion> reservaHabitaciones;
        List<DetalleHuesped> detalleHuespedes;

        if (request.habitaciones() != null && !request.habitaciones().isEmpty()) {
            // Caso A: el cliente reemplaza la lista de habitaciones
            long noches = ChronoUnit.DAYS.between(fechaInicio, fechaFin);
            List<Habitacion> habitaciones = cargarYValidarHabitaciones(request.habitaciones());
            List<TipoHabitacion> tipos    = cargarTipos(request.habitaciones());

            disponibilidadService.validarDisponibilidad(request.habitaciones(), fechaInicio, fechaFin, reservaId);

            // Update es staff-only (PUT /reservas): se valida el rango de tarifa pactada.
            List<ReservaHabitacionRequest> habNormalizadas = normalizarTarifas(request.habitaciones(), tipos, false);
            subtotalCalculado = calcularSubtotal(habNormalizadas, tipos, noches);

            reservaHabitacionRepository.findByReservaReservaId(reservaId)
                    .forEach(rh -> {
                        rh.setEstado(EstadoReservaHabitacion.LIBERADA);
                        reservaHabitacionRepository.save(rh);
                    });
            reservaHabitaciones = persistirHabitaciones(habNormalizadas, reserva, habitaciones, tipos, noches);

        } else if (fechasCambiaron) {
            // Caso B: solo cambian las fechas — revalidar disponibilidad y recalcular noches/subtotal
            List<ReservaHabitacion> existentes = reservaHabitacionRepository.findByReservaReservaId(reservaId);
            validarDisponibilidadExistentes(existentes, fechaInicio, fechaFin, reservaId);
            subtotalCalculado = recalcularHabitacionesExistentes(existentes, fechaInicio, fechaFin);
            reservaHabitaciones = existentes;

        } else {
            // Caso C: sin cambios en habitaciones ni fechas
            reservaHabitaciones = reservaHabitacionRepository.findByReservaReservaId(reservaId);
        }

        if (request.huespedes() != null && !request.huespedes().isEmpty()) {
            validarUnSoloPrincipal(request.huespedes());
            detalleHuespedRepository.deleteByIdReservaId(reservaId);
            detalleHuespedes = persistirHuespedes(request.huespedes(), reserva);
        } else {
            detalleHuespedes = detalleHuespedRepository.findByIdReservaId(reservaId);
        }

        // Recalcular SIEMPRE los montos server-side (impuesto, total, adelanto), ignorando
        // los montos del payload. Si no cambió el subtotal, se conserva el actual.
        BigDecimal subtotalFinal = subtotalCalculado != null ? subtotalCalculado : reserva.getSubtotal();
        BigDecimal descuentoReq  = request.descuento() != null ? request.descuento() : reserva.getDescuento();
        BigDecimal descuentoFinal = normalizarDescuento(descuentoReq, subtotalFinal, false);
        ModalidadPago modalidadFinal = request.modalidadPago() != null
                ? request.modalidadPago()
                : reserva.getModalidadPago();
        ReservaMontos montos = calcularMontos(reserva.getCodReserva(), subtotalFinal, descuentoFinal, modalidadFinal);

        reservaMapper.updateEntity(reserva, request, canal, montos);
        Reserva saved = reservaRepository.save(reserva);
        eventPublisher.publishUpdated(saved);
        return reservaMapper.toResponse(saved, reservaHabitaciones, detalleHuespedes);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaResponse findById(Integer reservaId) {
        Reserva reserva = obtenerOFallar(reservaId);
        List<ReservaHabitacion> habitaciones = reservaHabitacionRepository.findByReservaReservaId(reservaId);
        List<DetalleHuesped> huespedes = detalleHuespedRepository.findByIdReservaId(reservaId);
        Integer estanciaId = resolverEstanciaId(reservaId);
        return reservaMapper.toResponse(reserva, habitaciones, huespedes, estanciaId);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaResponse findByCodigo(String codReserva) {
        Reserva reserva = reservaRepository.findByCodReserva(codReserva)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada: " + codReserva));
        List<ReservaHabitacion> habitaciones = reservaHabitacionRepository.findByReservaReservaId(reserva.getReservaId());
        List<DetalleHuesped> huespedes = detalleHuespedRepository.findByIdReservaId(reserva.getReservaId());
        Integer estanciaId = resolverEstanciaId(reserva.getReservaId());
        return reservaMapper.toResponse(reserva, habitaciones, huespedes, estanciaId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResponse> search(ReservaFilterRequest filter, Pageable pageable) {
        Page<Reserva> page = reservaRepository.findAll(ReservaSpecification.build(filter), pageable);

        List<Integer> ids = page.stream().map(Reserva::getReservaId).toList();
        if (ids.isEmpty()) return page.map(reservaMapper::toResponse);

        // Batch: 2 queries extra en total, no N×2
        Map<Integer, List<ReservaHabitacion>> habPorReserva =
                reservaHabitacionRepository.findByReservaReservaIdIn(ids)
                        .stream()
                        .collect(Collectors.groupingBy(rh -> rh.getReserva().getReservaId()));

        Map<Integer, List<DetalleHuesped>> huesPorReserva =
                detalleHuespedRepository.findByIdReservaIdIn(ids)
                        .stream()
                        .collect(Collectors.groupingBy(dh -> dh.getId().getReservaId()));

        return page.map(r -> reservaMapper.toResponse(
                r,
                habPorReserva.getOrDefault(r.getReservaId(), Collections.emptyList()),
                huesPorReserva.getOrDefault(r.getReservaId(), Collections.emptyList())
        ));
    }

    @Override
    public ReservaResponse cambiarEstado(Integer reservaId, CambiarEstadoReservaRequest request) {
        Reserva reserva = obtenerOFallar(reservaId);
        EstadoReserva actual = reserva.getEstado();
        EstadoReserva nuevo = request.nuevoEstado();

        if (actual == nuevo) {
            throw new BusinessException("La reserva ya se encuentra en estado " + nuevo);
        }
        Set<EstadoReserva> permitidos = TRANSICIONES.getOrDefault(actual, Set.of());
        if (!permitidos.contains(nuevo)) {
            throw new BusinessException("Transición de estado no permitida: " + actual + " -> " + nuevo);
        }

        reserva.setEstado(nuevo);
        if (request.motivo() != null && !request.motivo().isBlank()) {
            String prefijo = reserva.getObservaciones() == null ? "" : reserva.getObservaciones() + "\n";
            reserva.setObservaciones(prefijo + "[" + nuevo + "] " + request.motivo());
        }

        Reserva saved = reservaRepository.save(reserva);
        registrarHistorial(saved, actual, nuevo, request.motivo());
        eventPublisher.publishStateChanged(saved);

        List<ReservaHabitacion> habitaciones = reservaHabitacionRepository.findByReservaReservaId(reservaId);
        List<DetalleHuesped> huespedes = detalleHuespedRepository.findByIdReservaId(reservaId);
        Integer estanciaId = resolverEstanciaId(reservaId);
        return reservaMapper.toResponse(saved, habitaciones, huespedes, estanciaId);
    }

    @Override
    public CancelacionResponse cancelar(Integer reservaId, CancelarReservaRequest request) {
        Reserva reserva = obtenerOFallar(reservaId);

        EstadoReserva estadoAntes = reserva.getEstado();
        Set<EstadoReserva> permitidos = TRANSICIONES.getOrDefault(estadoAntes, Set.of());
        if (!permitidos.contains(EstadoReserva.CANCELADA)) {
            throw new BusinessException(
                    "No se puede cancelar una reserva en estado " + estadoAntes);
        }

        // Calcular penalización según política de anticipación
        boolean aplicar = !Boolean.FALSE.equals(request.aplicarPenalizacion());
        PoliticaCancelacion politica = calcularPolitica(reserva.getFechaInicio(), aplicar);

        BigDecimal adelanto    = reserva.getAdelanto();
        BigDecimal penalizacion = adelanto
                .multiply(BigDecimal.valueOf(politica.porcentaje()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal devolucion  = adelanto.subtract(penalizacion);

        // Liberar todas las habitaciones
        reservaHabitacionRepository.findByReservaReservaId(reservaId).forEach(rh -> {
            rh.setEstado(EstadoReservaHabitacion.LIBERADA);
            reservaHabitacionRepository.save(rh);
        });

        // Registrar en observaciones
        String entrada = "[CANCELADA] " + request.motivo()
                + " | Política: " + politica.descripcion()
                + " | Penalización: S/ " + penalizacion
                + " | Devolución: S/ " + devolucion;
        String obs = reserva.getObservaciones() == null
                ? entrada : reserva.getObservaciones() + "\n" + entrada;
        reserva.setObservaciones(obs);
        reserva.setEstado(EstadoReserva.CANCELADA);

        Reserva saved = reservaRepository.save(reserva);
        registrarHistorial(saved, estadoAntes, EstadoReserva.CANCELADA, request.motivo());
        eventPublisher.publishStateChanged(saved);

        List<ReservaHabitacion> habitaciones = reservaHabitacionRepository.findByReservaReservaId(reservaId);
        List<DetalleHuesped> huespedes       = detalleHuespedRepository.findByIdReservaId(reservaId);
        Integer estanciaId                   = resolverEstanciaId(reservaId);
        ReservaResponse reservaResponse      = reservaMapper.toResponse(saved, habitaciones, huespedes, estanciaId);

        return new CancelacionResponse(reservaResponse, adelanto, penalizacion, devolucion, politica.descripcion());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialReservaResponse> obtenerHistorial(Integer reservaId) {
        obtenerOFallar(reservaId);
        return historialReservaRepository
                .findByReservaReservaIdOrderByFechaCreacionAsc(reservaId)
                .stream()
                .map(historialReservaMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResponse> findByUsuarioId(Integer usuarioId, Pageable pageable) {
        return reservaRepository.findByUsuarioUsuarioId(usuarioId, pageable)
                .map(reservaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaResponse findPropiaById(Integer reservaId, Integer usuarioId) {
        Reserva reserva = obtenerOFallar(reservaId);
        if (!reserva.getUsuario().getUsuarioId().equals(usuarioId)) {
            throw new BusinessException("No tienes permiso para ver esta reserva");
        }
        List<ReservaHabitacion> habitaciones = reservaHabitacionRepository.findByReservaReservaId(reservaId);
        List<DetalleHuesped> huespedes = detalleHuespedRepository.findByIdReservaId(reservaId);
        Integer estanciaId = resolverEstanciaId(reservaId);
        return reservaMapper.toResponse(reserva, habitaciones, huespedes, estanciaId);
    }

    @Override
    public CancelacionResponse cancelarPropiaReserva(Integer reservaId, Integer usuarioId,
                                                      CancelarReservaRequest request) {
        Reserva reserva = obtenerOFallar(reservaId);
        if (!reserva.getUsuario().getUsuarioId().equals(usuarioId)) {
            throw new BusinessException("No tienes permiso para cancelar esta reserva");
        }
        return cancelar(reservaId, request);
    }

    @Override
    public void deleteById(Integer reservaId) {
        Reserva reserva = obtenerOFallar(reservaId);
        if (reserva.getEstado() != EstadoReserva.PENDIENTE && reserva.getEstado() != EstadoReserva.CANCELADA) {
            throw new BusinessException("Solo se pueden eliminar reservas en estado PENDIENTE o CANCELADA");
        }
        reservaRepository.delete(reserva);
        eventPublisher.publishDeleted(reserva.getReservaId(), reserva.getCodReserva());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void registrarHistorial(Reserva reserva,
                                    EstadoReserva estadoAnterior,
                                    EstadoReserva estadoNuevo,
                                    String motivo) {
        HistorialReserva historial = historialReservaMapper.toEntity(reserva, estadoAnterior, estadoNuevo, motivo);
        historialReservaRepository.save(historial);
    }

    private List<Habitacion> cargarYValidarHabitaciones(List<ReservaHabitacionRequest> requests) {
        return requests.stream().map(req -> {
            Habitacion h = habitacionRepository.findById(req.habitacionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Habitación no encontrada: " + req.habitacionId()));
            if (h.getEstado() == EstadoHabitacion.MANTENIMIENTO || h.getEstado() == EstadoHabitacion.BLOQUEADA) {
                throw new BusinessException("La habitación " + h.getNumero() + " no está disponible para reservas (estado: " + h.getEstado() + ")");
            }
            return h;
        }).toList();
    }

    private List<TipoHabitacion> cargarTipos(List<ReservaHabitacionRequest> requests) {
        return requests.stream().map(req ->
                tipoHabitacionRepository.findById(req.tipoHabitacionId())
                        .orElseThrow(() -> new ResourceNotFoundException("Tipo de habitación no encontrado: " + req.tipoHabitacionId()))
        ).toList();
    }

    private BigDecimal calcularSubtotal(List<ReservaHabitacionRequest> requests,
                                        List<TipoHabitacion> tipos,
                                        long noches) {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < requests.size(); i++) {
            BigDecimal tarifa = requests.get(i).tarifaPactada() != null
                    ? requests.get(i).tarifaPactada()
                    : tipos.get(i).getPrecioBase();
            total = total.add(tarifa.multiply(BigDecimal.valueOf(noches)));
        }
        return total;
    }

    /**
     * Normaliza la tarifa pactada de cada habitación:
     *  - Cliente: se fuerza el precio base del tipo (no puede pactar tarifa).
     *  - Staff: si pacta tarifa, se valida que esté entre el 50% y el 200% del precio base.
     */
    private List<ReservaHabitacionRequest> normalizarTarifas(List<ReservaHabitacionRequest> requests,
                                                             List<TipoHabitacion> tipos,
                                                             boolean esCliente) {
        java.util.ArrayList<ReservaHabitacionRequest> out = new java.util.ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            ReservaHabitacionRequest req = requests.get(i);
            TipoHabitacion tipo = tipos.get(i);
            BigDecimal base = tipo.getPrecioBase();
            BigDecimal tarifa;
            if (esCliente || req.tarifaPactada() == null) {
                tarifa = base;
            } else {
                tarifa = req.tarifaPactada();
                BigDecimal min = base.multiply(TARIFA_MIN_RATIO);
                BigDecimal max = base.multiply(TARIFA_MAX_RATIO);
                if (tarifa.compareTo(min) < 0 || tarifa.compareTo(max) > 0) {
                    throw new ValidationException(
                            "La tarifa pactada no es válida para el tipo de habitación " + tipo.getNombre());
                }
            }
            out.add(new ReservaHabitacionRequest(req.habitacionId(), req.tipoHabitacionId(), tarifa));
        }
        return out;
    }

    /**
     * Descuento: el cliente nunca aplica descuento (=> 0). El staff puede aplicarlo,
     * con un tope del 30% del subtotal.
     */
    private BigDecimal normalizarDescuento(BigDecimal descuento, BigDecimal subtotal, boolean esCliente) {
        if (esCliente) {
            return BigDecimal.ZERO;
        }
        BigDecimal d = descuento != null ? descuento : BigDecimal.ZERO;
        BigDecimal max = subtotal.multiply(MAX_DESCUENTO_RATIO).setScale(2, RoundingMode.HALF_UP);
        if (d.compareTo(max) > 0) {
            throw new ValidationException("El descuento no puede superar el 30% del subtotal");
        }
        return d;
    }

    /**
     * Recalcula los montos server-side (fuente de verdad):
     *   impuesto   = subtotal × 18%
     *   montoTotal = max(0, subtotal − descuento + impuesto)
     *   adelanto   = TOTAL → montoTotal ; PARCIAL → 50% del montoTotal
     */
    private ReservaMontos calcularMontos(String codReserva, BigDecimal subtotal,
                                         BigDecimal descuento, ModalidadPago modalidad) {
        BigDecimal impuesto = subtotal.multiply(IGV).setScale(2, RoundingMode.HALF_UP);
        BigDecimal montoTotal = subtotal.subtract(descuento).add(impuesto).setScale(2, RoundingMode.HALF_UP);
        if (montoTotal.signum() < 0) {
            montoTotal = BigDecimal.ZERO;
        }
        ModalidadPago m = modalidad != null ? modalidad : ModalidadPago.PARCIAL;
        BigDecimal adelanto = m == ModalidadPago.TOTAL
                ? montoTotal
                : montoTotal.multiply(PORC_ADELANTO_PARCIAL).setScale(2, RoundingMode.HALF_UP);
        return new ReservaMontos(codReserva, subtotal, descuento, impuesto, montoTotal, adelanto, m);
    }

    /**
     * Si el llamador provee un código no vacío, valida unicidad y lo respeta;
     * en caso contrario genera uno server-side. El wizard (staff/cliente) envía null.
     */
    private String resolverCodReserva(String provisto) {
        if (provisto != null && !provisto.isBlank()) {
            if (reservaRepository.existsByCodReserva(provisto)) {
                throw new ConflictException("Ya existe una reserva con código " + provisto);
            }
            return provisto;
        }
        return generarCodReserva();
    }

    /**
     * Genera un código único con formato RSV-AAAA-NNNNNN, reintentando ante colisión.
     */
    private String generarCodReserva() {
        int anio = DateTimeUtils.today().getYear();
        String cod;
        do {
            int n = 100000 + RNG.nextInt(900000);
            cod = "RSV-" + anio + "-" + n;
        } while (reservaRepository.existsByCodReserva(cod));
        return cod;
    }

    private List<ReservaHabitacion> persistirHabitaciones(List<ReservaHabitacionRequest> requests,
                                                           Reserva reserva,
                                                           List<Habitacion> habitaciones,
                                                           List<TipoHabitacion> tipos,
                                                           long noches) {
        java.util.ArrayList<ReservaHabitacion> result = new java.util.ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            ReservaHabitacion rh = reservaHabitacionMapper.toEntity(
                    requests.get(i), reserva, habitaciones.get(i), tipos.get(i), noches);
            result.add(reservaHabitacionRepository.save(rh));
        }
        return result;
    }

    /**
     * Cuando solo cambian las fechas, revalida disponibilidad de las habitaciones ya asignadas
     * excluyendo la propia reserva para no bloquearse a sí misma.
     */
    private void validarDisponibilidadExistentes(List<ReservaHabitacion> existentes,
                                                  LocalDate fechaInicio,
                                                  LocalDate fechaFin,
                                                  Integer excluirReservaId) {
        Set<EstadoReserva> estadosLibera = Set.of(EstadoReserva.CANCELADA, EstadoReserva.NO_SHOW);
        for (ReservaHabitacion rh : existentes) {
            Integer habId = rh.getHabitacion().getHabitacionId();
            boolean ocupada = reservaHabitacionRepository.existeSolapamiento(
                    habId, fechaInicio, fechaFin, estadosLibera, excluirReservaId);
            if (ocupada) {
                throw new ConflictException(
                        "La habitación " + rh.getHabitacion().getNumero()
                        + " ya tiene una reserva activa entre " + fechaInicio + " y " + fechaFin);
            }
        }
    }

    /**
     * Recalcula noches y subtotal de cada ReservaHabitacion existente con las nuevas fechas
     * y devuelve el subtotal total actualizado.
     */
    private BigDecimal recalcularHabitacionesExistentes(List<ReservaHabitacion> existentes,
                                                         LocalDate fechaInicio,
                                                         LocalDate fechaFin) {
        long noches = ChronoUnit.DAYS.between(fechaInicio, fechaFin);
        BigDecimal subtotalTotal = BigDecimal.ZERO;
        for (ReservaHabitacion rh : existentes) {
            BigDecimal subtotal = rh.getTarifaPactada().multiply(BigDecimal.valueOf(noches));
            rh.setNoches((int) noches);
            rh.setSubtotal(subtotal);
            reservaHabitacionRepository.save(rh);
            subtotalTotal = subtotalTotal.add(subtotal);
        }
        return subtotalTotal;
    }

    private List<DetalleHuesped> persistirHuespedes(List<HuespedReservaRequest> requests, Reserva reserva) {
        return requests.stream().map(req -> {
            Huesped huesped = huespedRepository.findById(req.huespedId())
                    .orElseThrow(() -> new ResourceNotFoundException("Huésped no encontrado: " + req.huespedId()));
            DetalleHuesped detalle = detalleHuespedMapper.toEntity(req, reserva, huesped);
            return detalleHuespedRepository.save(detalle);
        }).toList();
    }

    private void validarFechas(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null) return;
        if (!fin.isAfter(inicio)) {
            throw new ValidationException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
    }

    private void detectarDuplicado(List<HuespedReservaRequest> huespedes,
                                    LocalDate fechaInicio, LocalDate fechaFin) {
        // Estados que no "liberan" la habitación — reservas que siguen activas
        Set<EstadoReserva> estadosLibera = Set.of(EstadoReserva.CANCELADA, EstadoReserva.NO_SHOW);

        huespedes.stream()
                .filter(HuespedReservaRequest::esPrincipal)
                .findFirst()
                .ifPresent(principal -> {
                    List<Reserva> solapadas = reservaRepository.findSolapadasPorHuespedPrincipal(
                            principal.huespedId(), fechaInicio, fechaFin, estadosLibera);
                    if (!solapadas.isEmpty()) {
                        String codigos = solapadas.stream()
                                .map(Reserva::getCodReserva)
                                .collect(Collectors.joining(", "));
                        throw new ConflictException(
                                "El huésped ya tiene reserva(s) activa(s) con fechas solapadas: ["
                                + codigos + "]. Use forzar=true para crear igualmente.");
                    }
                });
    }

    private void validarUnSoloPrincipal(List<HuespedReservaRequest> huespedes) {
        long principales = huespedes.stream().filter(HuespedReservaRequest::esPrincipal).count();
        if (principales != 1) {
            throw new ValidationException("La reserva debe tener exactamente un huésped principal (encontrados: " + principales + ")");
        }
    }

    private boolean esEstadoTerminal(EstadoReserva estado) {
        return estado == EstadoReserva.CHECK_OUT
                || estado == EstadoReserva.CANCELADA
                || estado == EstadoReserva.NO_SHOW;
    }

    private Reserva obtenerOFallar(Integer reservaId) {
        return reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada: " + reservaId));
    }

    /**
     * Política de penalización por anticipación al check-in:
     *  >= 7 días → 0 %   (cancelación anticipada)
     *   3-6 días → 25 %
     *   1-2 días → 50 %
     *    0 días  → 100 % (mismo día o fecha ya pasada)
     * Si aplicar=false se exonera la penalización (override de recepción).
     */
    private PoliticaCancelacion calcularPolitica(LocalDate fechaInicio, boolean aplicar) {
        if (!aplicar) {
            return new PoliticaCancelacion(0, "Sin penalización (exonerado por recepción)");
        }
        long dias = ChronoUnit.DAYS.between(DateTimeUtils.today(), fechaInicio);
        if (dias >= 7) return new PoliticaCancelacion(0,   "Sin penalización (≥7 días de anticipación)");
        if (dias >= 3) return new PoliticaCancelacion(25,  "Penalización del 25 % (3-6 días de anticipación)");
        if (dias >= 1) return new PoliticaCancelacion(50,  "Penalización del 50 % (1-2 días de anticipación)");
        return          new PoliticaCancelacion(100, "Penalización del 100 % (cancelación el mismo día o tardía)");
    }

    private record PoliticaCancelacion(int porcentaje, String descripcion) {}

    private Integer resolverEstanciaId(Integer reservaId) {
        return estanciaRepository.findByReservaReservaId(reservaId)
                .map(e -> e.getEstanciaId())
                .orElse(null);
    }
}
