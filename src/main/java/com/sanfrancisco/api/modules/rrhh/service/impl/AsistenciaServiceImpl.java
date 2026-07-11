package com.sanfrancisco.api.modules.rrhh.service.impl;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.rrhh.dto.request.AsistenciaFilterRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.CreateAsistenciaRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.UpdateAsistenciaRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.AsistenciaResponse;
import com.sanfrancisco.api.modules.rrhh.entity.Asistencia;
import com.sanfrancisco.api.modules.rrhh.entity.Turno;
import com.sanfrancisco.api.modules.rrhh.enums.EstadoTurno;
import com.sanfrancisco.api.modules.rrhh.enums.TipoAsistencia;
import com.sanfrancisco.api.modules.rrhh.mapper.AsistenciaMapper;
import com.sanfrancisco.api.modules.rrhh.repository.AsistenciaRepository;
import com.sanfrancisco.api.modules.rrhh.repository.TurnoRepository;
import com.sanfrancisco.api.modules.rrhh.service.interfaces.AsistenciaService;
import com.sanfrancisco.api.modules.rrhh.specification.AsistenciaSpecification;
import com.sanfrancisco.api.modules.rrhh.websocket.AsistenciaEventPublisher;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class AsistenciaServiceImpl implements AsistenciaService {

    /** Minutos de tolerancia antes de marcar TARDANZA respecto a la hora de inicio del turno. */
    private static final long TOLERANCIA_TARDANZA_MIN = 5;

    private final AsistenciaRepository asistenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TurnoRepository turnoRepository;
    private final AsistenciaMapper asistenciaMapper;
    private final AsistenciaEventPublisher eventPublisher;

    public AsistenciaServiceImpl(AsistenciaRepository asistenciaRepository,
                                 UsuarioRepository usuarioRepository,
                                 TurnoRepository turnoRepository,
                                 AsistenciaMapper asistenciaMapper,
                                 AsistenciaEventPublisher eventPublisher) {
        this.asistenciaRepository = asistenciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.turnoRepository = turnoRepository;
        this.asistenciaMapper = asistenciaMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public AsistenciaResponse create(CreateAsistenciaRequest request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + request.usuarioId()));

        Asistencia entity = asistenciaMapper.toEntity(request, usuario);
        Asistencia saved = asistenciaRepository.save(entity);
        eventPublisher.publishCreated(saved);
        return asistenciaMapper.toResponse(saved);
    }

    @Override
    public AsistenciaResponse update(Integer asistenciaId, UpdateAsistenciaRequest request) {
        Asistencia asistencia = obtenerOFallar(asistenciaId);

        asistenciaMapper.updateEntity(asistencia, request);

        if (asistencia.getHoraIngreso() != null && asistencia.getHoraEgreso() != null) {
            if (asistencia.getHoraEgreso().isBefore(asistencia.getHoraIngreso())) {
                // Validación para turnos en el mismo día. Si cruzan de día, la lógica del mapper suma 24h.
                // Aquí solo alertamos o podemos permitirlo. Lo permitiremos ya que el mapper suma 24h.
            }
        }

        Asistencia saved = asistenciaRepository.save(asistencia);
        eventPublisher.publishUpdated(saved);
        return asistenciaMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AsistenciaResponse findById(Integer asistenciaId) {
        return asistenciaMapper.toResponse(obtenerOFallar(asistenciaId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AsistenciaResponse> search(AsistenciaFilterRequest filter, Pageable pageable) {
        return asistenciaRepository.findAll(AsistenciaSpecification.build(filter), pageable)
                .map(asistenciaMapper::toResponse);
    }

    @Override
    public void deleteById(Integer asistenciaId) {
        Asistencia asistencia = obtenerOFallar(asistenciaId);
        asistenciaRepository.delete(asistencia);
        eventPublisher.publishDeleted(asistencia.getAsistenciaId());
    }

    // ─────────────────────── Marcado self-service ───────────────────────

    @Override
    public AsistenciaResponse marcarEntrada(Integer usuarioId) {
        LocalDate hoy = LocalDate.now();
        if (asistenciaRepository.existsByUsuarioUsuarioIdAndFecha(usuarioId, hoy)) {
            throw new BusinessException("Ya registraste tu entrada hoy");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + usuarioId));

        LocalTime ingreso = LocalTime.now().truncatedTo(ChronoUnit.SECONDS);

        // Cruce con el turno planificado del día (si existe y no está cancelado):
        // deriva TARDANZA y confirma el turno.
        Turno turno = turnoRepository.findByUsuarioUsuarioIdAndFecha(usuarioId, hoy)
                .filter(t -> t.getEstado() != EstadoTurno.CANCELADO)
                .orElse(null);

        TipoAsistencia tipo = TipoAsistencia.NORMAL;
        if (turno != null) {
            if (ingreso.isAfter(turno.getHoraInicio().plusMinutes(TOLERANCIA_TARDANZA_MIN))) {
                tipo = TipoAsistencia.TARDANZA;
            }
            turno.setEstado(EstadoTurno.CONFIRMADO);
        }

        Asistencia entity = Asistencia.builder()
                .fecha(hoy)
                .horaIngreso(ingreso)
                .tipo(tipo)
                .usuario(usuario)
                .turno(turno)
                .build();

        Asistencia saved = asistenciaRepository.save(entity);
        eventPublisher.publishCreated(saved);
        return asistenciaMapper.toResponse(saved);
    }

    @Override
    public AsistenciaResponse marcarSalida(Integer usuarioId) {
        LocalDate hoy = LocalDate.now();
        Asistencia asistencia = asistenciaRepository.findByUsuarioUsuarioIdAndFecha(usuarioId, hoy)
                .orElseThrow(() -> new BusinessException("No has registrado tu entrada hoy"));

        if (asistencia.getHoraEgreso() != null) {
            throw new BusinessException("Ya registraste tu salida hoy");
        }

        LocalTime egreso = LocalTime.now().truncatedTo(ChronoUnit.SECONDS);
        asistencia.setHoraEgreso(egreso);
        asistencia.setHorasTrabajadas(calcularHoras(asistencia.getHoraIngreso(), egreso));

        Asistencia saved = asistenciaRepository.save(asistencia);
        eventPublisher.publishUpdated(saved);
        return asistenciaMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsistenciaResponse> misAsistencias(Integer usuarioId) {
        return asistenciaRepository.findByUsuarioUsuarioIdOrderByFechaDesc(usuarioId).stream()
                .map(asistenciaMapper::toResponse)
                .toList();
    }

    /** Horas trabajadas entre ingreso y egreso, con soporte para turnos que cruzan medianoche. */
    private BigDecimal calcularHoras(LocalTime ingreso, LocalTime egreso) {
        long minutes = Duration.between(ingreso, egreso).toMinutes();
        if (minutes < 0) {
            minutes += 24 * 60;
        }
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private Asistencia obtenerOFallar(Integer asistenciaId) {
        return asistenciaRepository.findById(asistenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Asistencia no encontrada: " + asistenciaId));
    }
}
