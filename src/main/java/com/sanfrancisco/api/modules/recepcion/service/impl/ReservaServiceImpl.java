package com.sanfrancisco.api.modules.recepcion.service.impl;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.recepcion.dto.request.CambiarEstadoReservaRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.CreateReservaRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.ReservaFilterRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.UpdateReservaRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.ReservaResponse;
import com.sanfrancisco.api.modules.recepcion.entity.Canal;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import com.sanfrancisco.api.modules.recepcion.mapper.ReservaMapper;
import com.sanfrancisco.api.modules.recepcion.repository.CanalRepository;
import com.sanfrancisco.api.modules.recepcion.repository.ReservaRepository;
import com.sanfrancisco.api.modules.recepcion.service.interfaces.ReservaService;
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

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

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
    private final ReservaMapper reservaMapper;
    private final ReservaEventPublisher eventPublisher;

    public ReservaServiceImpl(ReservaRepository reservaRepository,
                              UsuarioRepository usuarioRepository,
                              CanalRepository canalRepository,
                              ReservaMapper reservaMapper,
                              ReservaEventPublisher eventPublisher) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.canalRepository = canalRepository;
        this.reservaMapper = reservaMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ReservaResponse create(CreateReservaRequest request) {
        validarFechas(request.fechaInicio(), request.fechaFin());

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

        Reserva entity = reservaMapper.toEntity(request, usuario, canal);
        Reserva saved = reservaRepository.save(entity);
        eventPublisher.publishCreated(saved);
        return reservaMapper.toResponse(saved);
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

        reservaMapper.updateEntity(reserva, request, canal);
        validarFechas(reserva.getFechaInicio(), reserva.getFechaFin());

        Reserva saved = reservaRepository.save(reserva);
        eventPublisher.publishUpdated(saved);
        return reservaMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaResponse findById(Integer reservaId) {
        return reservaMapper.toResponse(obtenerOFallar(reservaId));
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaResponse findByCodigo(String codReserva) {
        Reserva reserva = reservaRepository.findByCodReserva(codReserva)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada: " + codReserva));
        return reservaMapper.toResponse(reserva);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResponse> search(ReservaFilterRequest filter, Pageable pageable) {
        return reservaRepository.findAll(ReservaSpecification.build(filter), pageable)
                .map(reservaMapper::toResponse);
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
        eventPublisher.publishStateChanged(saved);
        return reservaMapper.toResponse(saved);
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

    private Reserva obtenerOFallar(Integer reservaId) {
        return reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada: " + reservaId));
    }

    private void validarFechas(java.time.LocalDate inicio, java.time.LocalDate fin) {
        if (inicio == null || fin == null) return;
        if (!fin.isAfter(inicio)) {
            throw new ValidationException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
    }

    private boolean esEstadoTerminal(EstadoReserva estado) {
        return estado == EstadoReserva.CHECK_OUT
                || estado == EstadoReserva.CANCELADA
                || estado == EstadoReserva.NO_SHOW;
    }
}
