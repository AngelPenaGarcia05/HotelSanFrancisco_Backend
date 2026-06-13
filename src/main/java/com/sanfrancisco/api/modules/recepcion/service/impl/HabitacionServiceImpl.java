package com.sanfrancisco.api.modules.recepcion.service.impl;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.recepcion.dto.request.CheckInRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.CheckOutRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.CreateHabitacionRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.UpdateHabitacionRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.CheckOutLiquidacionResponse;
import com.sanfrancisco.api.modules.recepcion.dto.response.HabitacionResponse;
import com.sanfrancisco.api.modules.recepcion.entity.Estancia;
import com.sanfrancisco.api.modules.recepcion.entity.Habitacion;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.recepcion.entity.ReservaHabitacion;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoHabitacion;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReservaHabitacion;
import com.sanfrancisco.api.modules.recepcion.mapper.HabitacionMapper;
import com.sanfrancisco.api.modules.recepcion.repository.EstanciaRepository;
import com.sanfrancisco.api.modules.recepcion.repository.HabitacionRepository;
import com.sanfrancisco.api.modules.recepcion.repository.ReservaHabitacionRepository;
import com.sanfrancisco.api.modules.recepcion.repository.ReservaRepository;
import com.sanfrancisco.api.modules.recepcion.service.interfaces.HabitacionService;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import com.sanfrancisco.api.shared.websocket.WebSocketEvent;
import com.sanfrancisco.api.shared.websocket.WebSocketPublisher;
import com.sanfrancisco.api.shared.websocket.WebSocketChannels;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class HabitacionServiceImpl implements HabitacionService {

    private final HabitacionRepository habitacionRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaHabitacionRepository reservaHabitacionRepository;
    private final EstanciaRepository estanciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HabitacionMapper mapper;
    private final WebSocketPublisher wsPublisher;

    public HabitacionServiceImpl(HabitacionRepository habitacionRepository,
                                 ReservaRepository reservaRepository,
                                 ReservaHabitacionRepository reservaHabitacionRepository,
                                 EstanciaRepository estanciaRepository,
                                 UsuarioRepository usuarioRepository,
                                 HabitacionMapper mapper,
                                 WebSocketPublisher wsPublisher) {
        this.habitacionRepository = habitacionRepository;
        this.reservaRepository = reservaRepository;
        this.reservaHabitacionRepository = reservaHabitacionRepository;
        this.estanciaRepository = estanciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.mapper = mapper;
        this.wsPublisher = wsPublisher;
    }

    // =========================================================================
    // CRUD
    // =========================================================================

    @Override
    public HabitacionResponse create(CreateHabitacionRequest request) {
        if (habitacionRepository.existsByNumero(request.numero().trim().toUpperCase())) {
            throw new BusinessException("Ya existe una habitación con el número: " + request.numero());
        }
        Habitacion saved = habitacionRepository.save(mapper.toEntity(request));
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

        // Validar que todas las habitaciones estén DISPONIBLE antes de proceder
        asignaciones.forEach(rh -> {
            EstadoHabitacion estadoActual = rh.getHabitacion().getEstado();
            if (estadoActual != EstadoHabitacion.DISPONIBLE) {
                throw new BusinessException(
                        "La habitación " + rh.getHabitacion().getNumero()
                        + " no está disponible (estado actual: " + estadoActual + ").");
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

        Habitacion primeraHabitacion = asignaciones.get(0).getHabitacion();
        broadcast("CHECKIN_REALIZADO", primeraHabitacion);
        return mapper.toResponse(primeraHabitacion);
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

        // Registrar fecha y personal de checkout en la estancia
        LocalDateTime ahora = LocalDateTime.now();
        Estancia estancia = estanciaRepository.findByReservaReservaId(reserva.getReservaId())
                .orElse(null);
        if (estancia != null) {
            estancia.setFechaCheckout(ahora);
            estancia.setUsuarioCheckout(usuario);
            estanciaRepository.save(estancia);
        }

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
                estancia != null ? estancia.getFechaCheckin() : null,
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

    private void broadcast(String tipo, Habitacion h) {
        wsPublisher.broadcast(WebSocketChannels.TOPIC_HABITACIONES,
                WebSocketEvent.of(tipo, "habitacion", mapper.toResponse(h)));
    }
}
