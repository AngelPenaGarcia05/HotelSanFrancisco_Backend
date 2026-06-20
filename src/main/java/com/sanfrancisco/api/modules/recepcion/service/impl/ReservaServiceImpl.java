package com.sanfrancisco.api.modules.recepcion.service.impl;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.recepcion.dto.request.*;
import com.sanfrancisco.api.modules.recepcion.dto.response.CancelacionResponse;
import com.sanfrancisco.api.modules.recepcion.dto.response.HistorialReservaResponse;
import com.sanfrancisco.api.modules.recepcion.dto.response.ReservaResponse;
import com.sanfrancisco.api.modules.recepcion.entity.*;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoHabitacion;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReservaHabitacion;
import com.sanfrancisco.api.modules.recepcion.mapper.DetalleHuespedMapper;
import com.sanfrancisco.api.modules.recepcion.mapper.HistorialReservaMapper;
import com.sanfrancisco.api.modules.recepcion.mapper.ReservaHabitacionMapper;
import com.sanfrancisco.api.modules.recepcion.mapper.ReservaMapper;
import com.sanfrancisco.api.modules.recepcion.repository.*;
import com.sanfrancisco.api.modules.recepcion.service.interfaces.DisponibilidadService;
import com.sanfrancisco.api.modules.recepcion.service.interfaces.ReservaService;

import java.util.Collections;
import com.sanfrancisco.api.modules.recepcion.specification.ReservaSpecification;
import com.sanfrancisco.api.modules.recepcion.websocket.ReservaEventPublisher;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import com.sanfrancisco.api.shared.exception.ConflictException;
import com.sanfrancisco.api.shared.exception.ValidationException;
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
    private final ReservaMapper reservaMapper;
    private final ReservaHabitacionMapper reservaHabitacionMapper;
    private final DetalleHuespedMapper detalleHuespedMapper;
    private final HistorialReservaMapper historialReservaMapper;
    private final HistorialReservaRepository historialReservaRepository;
    private final DisponibilidadService disponibilidadService;
    private final ReservaEventPublisher eventPublisher;

    public ReservaServiceImpl(ReservaRepository reservaRepository,
                              UsuarioRepository usuarioRepository,
                              CanalRepository canalRepository,
                              HabitacionRepository habitacionRepository,
                              TipoHabitacionRepository tipoHabitacionRepository,
                              HuespedRepository huespedRepository,
                              ReservaHabitacionRepository reservaHabitacionRepository,
                              DetalleHuespedRepository detalleHuespedRepository,
                              ReservaMapper reservaMapper,
                              ReservaHabitacionMapper reservaHabitacionMapper,
                              DetalleHuespedMapper detalleHuespedMapper,
                              HistorialReservaMapper historialReservaMapper,
                              HistorialReservaRepository historialReservaRepository,
                              DisponibilidadService disponibilidadService,
                              ReservaEventPublisher eventPublisher) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.canalRepository = canalRepository;
        this.habitacionRepository = habitacionRepository;
        this.tipoHabitacionRepository = tipoHabitacionRepository;
        this.huespedRepository = huespedRepository;
        this.reservaHabitacionRepository = reservaHabitacionRepository;
        this.detalleHuespedRepository = detalleHuespedRepository;
        this.reservaMapper = reservaMapper;
        this.reservaHabitacionMapper = reservaHabitacionMapper;
        this.detalleHuespedMapper = detalleHuespedMapper;
        this.historialReservaMapper = historialReservaMapper;
        this.historialReservaRepository = historialReservaRepository;
        this.disponibilidadService = disponibilidadService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ReservaResponse create(CreateReservaRequest request) {
        validarFechas(request.fechaInicio(), request.fechaFin());
        validarUnSoloPrincipal(request.huespedes());

        if (reservaRepository.existsByCodReserva(request.codReserva())) {
            throw new ConflictException("Ya existe una reserva con código " + request.codReserva());
        }

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

        BigDecimal subtotalCalculado = calcularSubtotal(habRequests, tipos, noches);

        Reserva entity = reservaMapper.toEntity(request, usuario, canal, subtotalCalculado);
        Reserva saved = reservaRepository.save(entity);

        List<ReservaHabitacion> reservaHabitaciones = persistirHabitaciones(habRequests, saved, habitaciones, tipos, noches);
        List<DetalleHuesped> detalleHuespedes = persistirHuespedes(request.huespedes(), saved);

        registrarHistorial(saved, null, EstadoReserva.PENDIENTE, "Alta de reserva");
        eventPublisher.publishCreated(saved);
        return reservaMapper.toResponse(saved, reservaHabitaciones, detalleHuespedes);
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

            subtotalCalculado = calcularSubtotal(request.habitaciones(), tipos, noches);

            reservaHabitacionRepository.findByReservaReservaId(reservaId)
                    .forEach(rh -> {
                        rh.setEstado(EstadoReservaHabitacion.LIBERADA);
                        reservaHabitacionRepository.save(rh);
                    });
            reservaHabitaciones = persistirHabitaciones(request.habitaciones(), reserva, habitaciones, tipos, noches);

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

        reservaMapper.updateEntity(reserva, request, canal, subtotalCalculado);
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
        return reservaMapper.toResponse(reserva, habitaciones, huespedes);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaResponse findByCodigo(String codReserva) {
        Reserva reserva = reservaRepository.findByCodReserva(codReserva)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada: " + codReserva));
        List<ReservaHabitacion> habitaciones = reservaHabitacionRepository.findByReservaReservaId(reserva.getReservaId());
        List<DetalleHuesped> huespedes = detalleHuespedRepository.findByIdReservaId(reserva.getReservaId());
        return reservaMapper.toResponse(reserva, habitaciones, huespedes);
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
        return reservaMapper.toResponse(saved, habitaciones, huespedes);
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
        ReservaResponse reservaResponse      = reservaMapper.toResponse(saved, habitaciones, huespedes);

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
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), fechaInicio);
        if (dias >= 7) return new PoliticaCancelacion(0,   "Sin penalización (≥7 días de anticipación)");
        if (dias >= 3) return new PoliticaCancelacion(25,  "Penalización del 25 % (3-6 días de anticipación)");
        if (dias >= 1) return new PoliticaCancelacion(50,  "Penalización del 50 % (1-2 días de anticipación)");
        return          new PoliticaCancelacion(100, "Penalización del 100 % (cancelación el mismo día o tardía)");
    }

    private record PoliticaCancelacion(int porcentaje, String descripcion) {}
}
