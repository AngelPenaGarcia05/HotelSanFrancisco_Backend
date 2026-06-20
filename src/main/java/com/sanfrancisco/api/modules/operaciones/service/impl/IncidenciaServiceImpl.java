package com.sanfrancisco.api.modules.operaciones.service.impl;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.operaciones.dto.request.CambiarEstadoIncidenciaRequest;
import com.sanfrancisco.api.modules.operaciones.dto.request.CreateIncidenciaRequest;
import com.sanfrancisco.api.modules.operaciones.dto.request.IncidenciaFilterRequest;
import com.sanfrancisco.api.modules.operaciones.dto.request.UpdateIncidenciaRequest;
import com.sanfrancisco.api.modules.operaciones.dto.response.IncidenciaResponse;
import com.sanfrancisco.api.modules.operaciones.entity.Incidencia;
import com.sanfrancisco.api.modules.operaciones.enums.EstadoIncidencia;
import com.sanfrancisco.api.modules.operaciones.mapper.IncidenciaMapper;
import com.sanfrancisco.api.modules.operaciones.repository.IncidenciaRepository;
import com.sanfrancisco.api.modules.operaciones.service.interfaces.IncidenciaService;
import com.sanfrancisco.api.modules.operaciones.specification.IncidenciaSpecification;
import com.sanfrancisco.api.modules.operaciones.websocket.IncidenciaEventPublisher;
import com.sanfrancisco.api.modules.recepcion.entity.Habitacion;
import com.sanfrancisco.api.modules.recepcion.entity.ReservaHabitacion;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoHabitacion;
import com.sanfrancisco.api.modules.recepcion.repository.HabitacionRepository;
import com.sanfrancisco.api.modules.recepcion.repository.ReservaHabitacionRepository;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class IncidenciaServiceImpl implements IncidenciaService {

    private static final Map<EstadoIncidencia, Set<EstadoIncidencia>> TRANSICIONES = new EnumMap<>(EstadoIncidencia.class);

    static {
        TRANSICIONES.put(EstadoIncidencia.ABIERTA, Set.of(EstadoIncidencia.EN_PROCESO, EstadoIncidencia.CERRADA));
        TRANSICIONES.put(EstadoIncidencia.EN_PROCESO, Set.of(EstadoIncidencia.RESUELTA, EstadoIncidencia.CERRADA));
        TRANSICIONES.put(EstadoIncidencia.RESUELTA, Set.of(EstadoIncidencia.CERRADA, EstadoIncidencia.EN_PROCESO));
        TRANSICIONES.put(EstadoIncidencia.CERRADA, Set.of());
    }

    private final IncidenciaRepository incidenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaHabitacionRepository reservaHabitacionRepository;
    private final HabitacionRepository habitacionRepository;
    private final IncidenciaMapper incidenciaMapper;
    private final IncidenciaEventPublisher eventPublisher;

    public IncidenciaServiceImpl(IncidenciaRepository incidenciaRepository,
                                 UsuarioRepository usuarioRepository,
                                 ReservaHabitacionRepository reservaHabitacionRepository,
                                 HabitacionRepository habitacionRepository,
                                 IncidenciaMapper incidenciaMapper,
                                 IncidenciaEventPublisher eventPublisher) {
        this.incidenciaRepository = incidenciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.reservaHabitacionRepository = reservaHabitacionRepository;
        this.habitacionRepository = habitacionRepository;
        this.incidenciaMapper = incidenciaMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public IncidenciaResponse create(CreateIncidenciaRequest request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + request.usuarioId()));

        ReservaHabitacion reservaHabitacion = null;
        if (request.reservaHabitacionId() != null) {
            reservaHabitacion = reservaHabitacionRepository.findById(request.reservaHabitacionId())
                    .orElseThrow(() -> new ResourceNotFoundException("ReservaHabitacion no encontrada: " + request.reservaHabitacionId()));
        }

        Incidencia saved = incidenciaRepository.save(incidenciaMapper.toEntity(request, usuario, reservaHabitacion));

        // Incidencia ALTA con habitación asociada → bloquear habitación para mantenimiento
        if (request.prioridad() == com.sanfrancisco.api.modules.operaciones.enums.PrioridadIncidencia.ALTA
                && reservaHabitacion != null) {
            Habitacion hab = reservaHabitacion.getHabitacion();
            if (hab.getEstado() != EstadoHabitacion.MANTENIMIENTO) {
                hab.setEstado(EstadoHabitacion.MANTENIMIENTO);
                habitacionRepository.save(hab);
            }
        }

        eventPublisher.publishCreated(saved);
        return incidenciaMapper.toResponse(saved);
    }

    @Override
    public IncidenciaResponse update(Integer incidenciaId, UpdateIncidenciaRequest request) {
        Incidencia incidencia = obtenerOFallar(incidenciaId);

        if (incidencia.getEstado() == EstadoIncidencia.CERRADA) {
            throw new BusinessException("No se puede modificar una incidencia CERRADA");
        }

        ReservaHabitacion reservaHabitacion = null;
        if (request.reservaHabitacionId() != null) {
            reservaHabitacion = reservaHabitacionRepository.findById(request.reservaHabitacionId())
                    .orElseThrow(() -> new ResourceNotFoundException("ReservaHabitacion no encontrada: " + request.reservaHabitacionId()));
        }

        incidenciaMapper.updateEntity(incidencia, request, reservaHabitacion);
        Incidencia saved = incidenciaRepository.save(incidencia);
        eventPublisher.publishUpdated(saved);
        return incidenciaMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public IncidenciaResponse findById(Integer incidenciaId) {
        return incidenciaMapper.toResponse(obtenerOFallar(incidenciaId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IncidenciaResponse> search(IncidenciaFilterRequest filter, Pageable pageable) {
        return incidenciaRepository.findAll(IncidenciaSpecification.build(filter), pageable)
                .map(incidenciaMapper::toResponse);
    }

    @Override
    public IncidenciaResponse cambiarEstado(Integer incidenciaId, CambiarEstadoIncidenciaRequest request) {
        Incidencia incidencia = obtenerOFallar(incidenciaId);
        EstadoIncidencia actual = incidencia.getEstado();
        EstadoIncidencia nuevo = request.nuevoEstado();

        if (actual == nuevo) {
            throw new BusinessException("La incidencia ya se encuentra en estado " + nuevo);
        }
        Set<EstadoIncidencia> permitidos = TRANSICIONES.getOrDefault(actual, Set.of());
        if (!permitidos.contains(nuevo)) {
            throw new BusinessException("Transición de estado no permitida: " + actual + " -> " + nuevo);
        }

        if (nuevo == EstadoIncidencia.RESUELTA || nuevo == EstadoIncidencia.CERRADA) {
            if (request.solucion() != null && !request.solucion().isBlank()) {
                incidencia.setSolucion(request.solucion());
            }
            if (incidencia.getFechaResolucion() == null) {
                incidencia.setFechaResolucion(LocalDateTime.now());
            }
        }

        incidencia.setEstado(nuevo);
        Incidencia saved = incidenciaRepository.save(incidencia);
        eventPublisher.publishStateChanged(saved);
        return incidenciaMapper.toResponse(saved);
    }

    @Override
    public void deleteById(Integer incidenciaId) {
        Incidencia incidencia = obtenerOFallar(incidenciaId);
        if (incidencia.getEstado() != EstadoIncidencia.ABIERTA && incidencia.getEstado() != EstadoIncidencia.CERRADA) {
            throw new BusinessException("Solo se pueden eliminar incidencias en estado ABIERTA o CERRADA");
        }
        incidenciaRepository.delete(incidencia);
        eventPublisher.publishDeleted(incidencia.getIncidenciaId());
    }

    private Incidencia obtenerOFallar(Integer incidenciaId) {
        return incidenciaRepository.findById(incidenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Incidencia no encontrada: " + incidenciaId));
    }
}
