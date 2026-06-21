package com.sanfrancisco.api.modules.recepcion.service.impl;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.recepcion.dto.request.CheckInRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.CheckOutRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.CreateHabitacionRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.UpdateHabitacionRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.CalendarioHabitacionResponse;
import com.sanfrancisco.api.modules.recepcion.dto.response.CheckOutLiquidacionResponse;
import com.sanfrancisco.api.modules.recepcion.dto.response.HabitacionResponse;
import com.sanfrancisco.api.modules.recepcion.entity.Estancia;
import com.sanfrancisco.api.modules.recepcion.entity.Habitacion;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.recepcion.entity.ReservaHabitacion;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoHabitacion;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReservaHabitacion;
import com.sanfrancisco.api.modules.recepcion.entity.TipoHabitacion;
import com.sanfrancisco.api.modules.recepcion.mapper.HabitacionMapper;
import com.sanfrancisco.api.modules.recepcion.mapper.HistorialReservaMapper;
import com.sanfrancisco.api.modules.recepcion.repository.EstanciaRepository;
import com.sanfrancisco.api.modules.recepcion.repository.HabitacionRepository;
import com.sanfrancisco.api.modules.recepcion.repository.HistorialReservaRepository;
import com.sanfrancisco.api.modules.recepcion.repository.ReservaHabitacionRepository;
import com.sanfrancisco.api.modules.recepcion.repository.ReservaRepository;
import com.sanfrancisco.api.modules.recepcion.repository.TipoHabitacionRepository;
import com.sanfrancisco.api.modules.recepcion.service.interfaces.HabitacionService;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import com.sanfrancisco.api.shared.websocket.WebSocketEvent;
import com.sanfrancisco.api.shared.websocket.WebSocketPublisher;
import com.sanfrancisco.api.shared.websocket.WebSocketChannels;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class HabitacionServiceImpl implements HabitacionService {

    private final HabitacionRepository habitacionRepository;
    private final TipoHabitacionRepository tipoHabitacionRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaHabitacionRepository reservaHabitacionRepository;
    private final EstanciaRepository estanciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HabitacionMapper mapper;
    private final WebSocketPublisher wsPublisher;
    private final HistorialReservaRepository historialReservaRepository;
    private final HistorialReservaMapper historialReservaMapper;

    public HabitacionServiceImpl(HabitacionRepository habitacionRepository,
                                 TipoHabitacionRepository tipoHabitacionRepository,
                                 ReservaRepository reservaRepository,
                                 ReservaHabitacionRepository reservaHabitacionRepository,
                                 EstanciaRepository estanciaRepository,
                                 UsuarioRepository usuarioRepository,
                                 HabitacionMapper mapper,
                                 WebSocketPublisher wsPublisher,
                                 HistorialReservaRepository historialReservaRepository,
                                 HistorialReservaMapper historialReservaMapper) {
        this.habitacionRepository = habitacionRepository;
        this.tipoHabitacionRepository = tipoHabitacionRepository;
        this.reservaRepository = reservaRepository;
        this.reservaHabitacionRepository = reservaHabitacionRepository;
        this.estanciaRepository = estanciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.mapper = mapper;
        this.wsPublisher = wsPublisher;
        this.historialReservaRepository = historialReservaRepository;
        this.historialReservaMapper = historialReservaMapper;
    }

    // =========================================================================
    // CRUD
    // =========================================================================

    @Override
    public HabitacionResponse create(CreateHabitacionRequest request) {
        if (habitacionRepository.existsByNumero(request.numero().trim().toUpperCase())) {
            throw new BusinessException("Ya existe una habitación con el número: " + request.numero());
        }
        Habitacion habitacion = mapper.toEntity(request);
        if (request.tipoHabitacionId() != null) {
            TipoHabitacion tipo = tipoHabitacionRepository.findById(request.tipoHabitacionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de habitación no encontrado: " + request.tipoHabitacionId()));
            habitacion.setTipoHabitacion(tipo);
        }
        Habitacion saved = habitacionRepository.save(habitacion);
        broadcast("HABITACION_CREADA", saved);
        return mapper.toResponse(saved);
    }

    @Override
    public HabitacionResponse update(Integer habitacionId, UpdateHabitacionRequest request) {
        Habitacion habitacion = obtenerOFallar(habitacionId);

        if (request.numero() != null) {
            String nuevoNumero = request.numero().trim().toUpperCase();
            if (!nuevoNumero.equals(habitacion.getNumero()) && habitacionRepository.existsByNumero(nuevoNumero)) {
                throw new BusinessException("Ya existe una habitación con el número: " + nuevoNumero);
            }
        }

        mapper.updateEntity(habitacion, request);
        if (request.tipoHabitacionId() != null) {
            TipoHabitacion tipo = tipoHabitacionRepository.findById(request.tipoHabitacionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de habitación no encontrado: " + request.tipoHabitacionId()));
            habitacion.setTipoHabitacion(tipo);
        }
        Habitacion saved = habitacionRepository.save(habitacion);
        broadcast("HABITACION_ACTUALIZADA", saved);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public HabitacionResponse findById(Integer habitacionId) {
        return mapper.toResponse(obtenerOFallar(habitacionId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HabitacionResponse> findAll() {
        return habitacionRepository.findAllByOrderByPisoAscNumeroAsc()
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HabitacionResponse> findByEstado(EstadoHabitacion estado) {
        return habitacionRepository.findByEstado(estado)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HabitacionResponse> findByPiso(Integer piso) {
        return habitacionRepository.findByPiso(piso)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HabitacionResponse> findLimpieza() {
        return habitacionRepository.findByEstado(EstadoHabitacion.LIMPIEZA)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    public HabitacionResponse cambiarEstado(Integer habitacionId, EstadoHabitacion nuevoEstado) {
        Habitacion habitacion = obtenerOFallar(habitacionId);
        if (habitacion.getEstado() == nuevoEstado) {
            throw new BusinessException("La habitación ya se encuentra en estado " + nuevoEstado);
        }
        habitacion.setEstado(nuevoEstado);
        Habitacion saved = habitacionRepository.save(habitacion);
        broadcast("HABITACION_CAMBIO_ESTADO", saved);
        return mapper.toResponse(saved);
    }

    @Override
    public void deleteById(Integer habitacionId) {
        Habitacion habitacion = obtenerOFallar(habitacionId);
        if (habitacion.getEstado() == EstadoHabitacion.OCUPADA) {
            throw new BusinessException("No se puede eliminar una habitación OCUPADA.");
        }
        habitacionRepository.delete(habitacion);
        wsPublisher.broadcast(WebSocketChannels.TOPIC_HABITACIONES,
                WebSocketEvent.of("HABITACION_ELIMINADA", "habitacion", habitacionId));
    }

    // =========================================================================
    // CHECK-IN
    // Transición: Reserva CONFIRMADA → CHECK_IN
    //             Habitaciones DISPONIBLE → OCUPADA
    //             ReservaHabitacion RESERVADA → OCUPADA
    //             Crea registro Estancia
    // =========================================================================
    @Override
    public HabitacionResponse checkIn(CheckInRequest request) {
        Reserva reserva = reservaRepository.findById(request.reservaId())
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada: " + request.reservaId()));

        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new BusinessException(
                    "Solo se puede hacer check-in de reservas CONFIRMADAS. Estado actual: " + reserva.getEstado());
        }

        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + request.usuarioId()));

        List<ReservaHabitacion> asignaciones = reservaHabitacionRepository.findByReservaReservaId(reserva.getReservaId());
        if (asignaciones.isEmpty()) {
            throw new BusinessException("La reserva no tiene habitaciones asignadas.");
        }

        // Validar que todas las habitaciones estén DISPONIBLE y en estado RESERVADA antes de proceder
        asignaciones.forEach(rh -> {
            EstadoHabitacion estadoActual = rh.getHabitacion().getEstado();
            if (estadoActual != EstadoHabitacion.DISPONIBLE) {
                throw new BusinessException(
                        "La habitación " + rh.getHabitacion().getNumero()
                        + " no está disponible (estado actual: " + estadoActual + ").");
            }
            if (rh.getEstado() != EstadoReservaHabitacion.RESERVADA) {
                throw new BusinessException(
                        "La asignación de la habitación " + rh.getHabitacion().getNumero()
                        + " no está en estado RESERVADA (estado actual: " + rh.getEstado() + ").");
            }
        });

        // Transición atómica de estados
        asignaciones.forEach(rh -> {
            rh.setEstado(EstadoReservaHabitacion.OCUPADA);
            reservaHabitacionRepository.save(rh);

            Habitacion hab = rh.getHabitacion();
            hab.setEstado(EstadoHabitacion.OCUPADA);
            habitacionRepository.save(hab);
        });

        reserva.setEstado(EstadoReserva.CHECK_IN);
        if (request.observaciones() != null && !request.observaciones().isBlank()) {
            reserva.setObservaciones(request.observaciones());
        }
        reservaRepository.save(reserva);

        // Crear registro de estancia
        estanciaRepository.save(Estancia.builder()
                .fechaCheckin(LocalDateTime.now())
                .usuarioCheckin(usuario)
                .reserva(reserva)
                .build());

        registrarHistorial(reserva, EstadoReserva.CONFIRMADA, EstadoReserva.CHECK_IN, request.observaciones());

        // Notificar todas las habitaciones del check-in
        asignaciones.forEach(rh -> broadcast("CHECKIN_REALIZADO", rh.getHabitacion()));
        return mapper.toResponse(asignaciones.get(0).getHabitacion());
    }

    // =========================================================================
    // CHECK-OUT
    // Transición: Reserva CHECK_IN → CHECK_OUT
    //             Habitaciones OCUPADA → LIMPIEZA
    //             ReservaHabitacion OCUPADA → LIBERADA
    //             Actualiza Estancia con fecha y usuario checkout
    //             Calcula liquidación: subtotal - descuento + impuesto + consumosAdicionales
    // =========================================================================
    @Override
    public CheckOutLiquidacionResponse checkOut(CheckOutRequest request) {
        Reserva reserva = reservaRepository.findById(request.reservaId())
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada: " + request.reservaId()));

        if (reserva.getEstado() != EstadoReserva.CHECK_IN) {
            throw new BusinessException(
                    "Solo se puede hacer check-out de reservas en CHECK_IN. Estado actual: " + reserva.getEstado());
        }

        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + request.usuarioId()));

        List<ReservaHabitacion> asignaciones = reservaHabitacionRepository.findByReservaReservaId(reserva.getReservaId());

        // Validar que todas las asignaciones estén OCUPADA antes de liberar
        asignaciones.forEach(rh -> {
            if (rh.getEstado() != EstadoReservaHabitacion.OCUPADA) {
                throw new BusinessException(
                        "La asignación de la habitación " + rh.getHabitacion().getNumero()
                        + " no está en estado OCUPADA (estado actual: " + rh.getEstado() + ").");
            }
        });

        // Liberar habitaciones — estado LIMPIEZA para activar flujo de housekeeping
        asignaciones.forEach(rh -> {
            rh.setEstado(EstadoReservaHabitacion.LIBERADA);
            reservaHabitacionRepository.save(rh);

            Habitacion hab = rh.getHabitacion();
            hab.setEstado(EstadoHabitacion.LIMPIEZA);
            habitacionRepository.save(hab);
        });

        // Actualizar estado de la reserva
        reserva.setEstado(EstadoReserva.CHECK_OUT);
        if (request.observaciones() != null && !request.observaciones().isBlank()) {
            reserva.setObservaciones(request.observaciones());
        }

        // Liquidación: agregar consumos adicionales al monto total
        BigDecimal consumos = request.consumosAdicionales() != null
                ? request.consumosAdicionales() : BigDecimal.ZERO;
        BigDecimal montoFinal = reserva.getMontoTotal().add(consumos);
        reserva.setMontoTotal(montoFinal);
        reservaRepository.save(reserva);

        // Registrar fecha y personal de checkout en la estancia (debe existir desde el check-in)
        LocalDateTime ahora = LocalDateTime.now();
        Estancia estancia = estanciaRepository.findByReservaReservaId(reserva.getReservaId())
                .orElseThrow(() -> new BusinessException(
                        "No se encontró el registro de estancia para la reserva: " + reserva.getCodReserva()));
        estancia.setFechaCheckout(ahora);
        estancia.setUsuarioCheckout(usuario);
        estanciaRepository.save(estancia);

        registrarHistorial(reserva, EstadoReserva.CHECK_IN, EstadoReserva.CHECK_OUT, request.observaciones());

        // Calcular noches de estancia
        long noches = ChronoUnit.DAYS.between(reserva.getFechaInicio(), reserva.getFechaFin());

        wsPublisher.broadcast(WebSocketChannels.TOPIC_HABITACIONES,
                WebSocketEvent.of("CHECKOUT_REALIZADO", "reserva", reserva.getReservaId()));

        return new CheckOutLiquidacionResponse(
                reserva.getReservaId(),
                reserva.getCodReserva(),
                reserva.getSubtotal(),
                reserva.getDescuento(),
                reserva.getImpuesto(),
                consumos,
                montoFinal,
                reserva.getAdelanto(),
                montoFinal.subtract(reserva.getAdelanto()),
                estancia.getFechaCheckin(),
                ahora,
                (int) noches
        );
    }

    // =========================================================================
    // LIMPIEZA COMPLETADA
    // Transición: LIMPIEZA → DISPONIBLE
    // =========================================================================
    @Override
    public HabitacionResponse registrarLimpiezaCompletada(Integer habitacionId) {
        Habitacion habitacion = obtenerOFallar(habitacionId);

        if (habitacion.getEstado() != EstadoHabitacion.LIMPIEZA) {
            throw new BusinessException(
                    "La habitación no está en estado LIMPIEZA (estado actual: " + habitacion.getEstado() + ").");
        }

        habitacion.setEstado(EstadoHabitacion.DISPONIBLE);
        Habitacion saved = habitacionRepository.save(habitacion);
        broadcast("LIMPIEZA_COMPLETADA", saved);
        return mapper.toResponse(saved);
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private Habitacion obtenerOFallar(Integer habitacionId) {
        return habitacionRepository.findById(habitacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Habitación no encontrada: " + habitacionId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarioHabitacionResponse> getCalendario(LocalDate fechaInicio, LocalDate fechaFin) {
        List<EstadoReserva> excluidos = List.of(EstadoReserva.CANCELADA, EstadoReserva.NO_SHOW);

        Map<Integer, List<ReservaHabitacion>> porHabitacion = reservaHabitacionRepository
                .findEnRango(fechaInicio, fechaFin, excluidos)
                .stream()
                .collect(Collectors.groupingBy(rh -> rh.getHabitacion().getHabitacionId()));

        return habitacionRepository.findAll().stream()
                .sorted(Comparator.comparing(Habitacion::getNumero))
                .map(hab -> {
                    List<CalendarioHabitacionResponse.CalendarioReservaItem> reservas =
                            porHabitacion.getOrDefault(hab.getHabitacionId(), List.of())
                                    .stream()
                                    .map(rh -> {
                                        Reserva res = rh.getReserva();
                                        Usuario u = res.getUsuario();
                                        String nombreHuesped = (u.getNombre() + " " + u.getApellidoPaterno()).trim();
                                        return new CalendarioHabitacionResponse.CalendarioReservaItem(
                                                res.getReservaId(),
                                                res.getCodReserva(),
                                                nombreHuesped,
                                                res.getFechaInicio(),
                                                res.getFechaFin(),
                                                res.getEstado()
                                        );
                                    })
                                    .sorted(Comparator.comparing(
                                            CalendarioHabitacionResponse.CalendarioReservaItem::fechaInicio))
                                    .toList();

                    TipoHabitacion tipo = hab.getTipoHabitacion();
                    return new CalendarioHabitacionResponse(
                            hab.getHabitacionId(),
                            hab.getNumero(),
                            hab.getPiso(),
                            tipo != null ? tipo.getNombre() : null,
                            tipo != null ? tipo.getPrecioBase() : null,
                            hab.getEstado(),
                            reservas
                    );
                })
                .toList();
    }

    private void registrarHistorial(Reserva reserva, EstadoReserva anterior, EstadoReserva nuevo, String motivo) {
        historialReservaRepository.save(historialReservaMapper.toEntity(reserva, anterior, nuevo, motivo));
    }

    private void broadcast(String tipo, Habitacion h) {
        wsPublisher.broadcast(WebSocketChannels.TOPIC_HABITACIONES,
                WebSocketEvent.of(tipo, "habitacion", mapper.toResponse(h)));
    }
}
