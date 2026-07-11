package com.sanfrancisco.api.modules.rrhh.service.impl;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.rrhh.dto.request.ActualizarTurnoRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.GenerarTurnosRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.GenerarTurnosResponse;
import com.sanfrancisco.api.modules.rrhh.dto.response.TurnoResponse;
import com.sanfrancisco.api.modules.rrhh.entity.DetalleHorario;
import com.sanfrancisco.api.modules.rrhh.entity.Horario;
import com.sanfrancisco.api.modules.rrhh.entity.Turno;
import com.sanfrancisco.api.modules.rrhh.enums.EstadoTurno;
import com.sanfrancisco.api.modules.rrhh.enums.OrigenTurno;
import com.sanfrancisco.api.modules.rrhh.mapper.TurnoMapper;
import com.sanfrancisco.api.modules.rrhh.repository.DetalleHorarioRepository;
import com.sanfrancisco.api.modules.rrhh.repository.HorarioRepository;
import com.sanfrancisco.api.modules.rrhh.repository.TurnoRepository;
import com.sanfrancisco.api.modules.rrhh.service.interfaces.TurnoService;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class TurnoServiceImpl implements TurnoService {

    private static final long RANGO_MAXIMO_DIAS = 366;

    private final TurnoRepository turnoRepository;
    private final DetalleHorarioRepository detalleHorarioRepository;
    private final HorarioRepository horarioRepository;
    private final TurnoMapper turnoMapper;

    public TurnoServiceImpl(TurnoRepository turnoRepository,
                            DetalleHorarioRepository detalleHorarioRepository,
                            HorarioRepository horarioRepository,
                            TurnoMapper turnoMapper) {
        this.turnoRepository = turnoRepository;
        this.detalleHorarioRepository = detalleHorarioRepository;
        this.horarioRepository = horarioRepository;
        this.turnoMapper = turnoMapper;
    }

    @Override
    public GenerarTurnosResponse generar(GenerarTurnosRequest request) {
        LocalDate desde = request.desde();
        LocalDate hasta = request.hasta();
        if (hasta.isBefore(desde)) {
            throw new BusinessException("La fecha 'hasta' no puede ser anterior a 'desde'");
        }
        if (ChronoUnit.DAYS.between(desde, hasta) > RANGO_MAXIMO_DIAS) {
            throw new BusinessException("El rango no puede exceder un año");
        }

        List<DetalleHorario> plantilla = detalleHorarioRepository.findByEstado(EstadoActivo.ACTIVO);
        List<TurnoResponse> creados = new ArrayList<>();
        int generados = 0;
        int omitidos = 0;

        for (LocalDate fecha = desde; !fecha.isAfter(hasta); fecha = fecha.plusDays(1)) {
            int diaSemana = fecha.getDayOfWeek().getValue(); // 1=Lunes .. 7=Domingo
            for (DetalleHorario dh : plantilla) {
                if (!dh.getDiaSemana().equals(diaSemana)) {
                    continue;
                }
                if (fecha.isBefore(dh.getFechaVigenciaInicio())) {
                    continue;
                }
                if (dh.getFechaVigenciaFin() != null && fecha.isAfter(dh.getFechaVigenciaFin())) {
                    continue;
                }
                Integer usuarioId = dh.getUsuario().getUsuarioId();
                if (turnoRepository.existsByUsuarioUsuarioIdAndFecha(usuarioId, fecha)) {
                    omitidos++;
                    continue;
                }
                Horario h = dh.getHorario();
                Turno turno = Turno.builder()
                        .usuario(dh.getUsuario())
                        .horario(h)
                        .fecha(fecha)
                        .horaInicio(h.getHoraEntrada())
                        .horaFin(h.getHoraSalida())
                        .estado(EstadoTurno.PLANIFICADO)
                        .origen(OrigenTurno.PLANTILLA)
                        .build();
                creados.add(turnoMapper.toResponse(turnoRepository.save(turno)));
                generados++;
            }
        }
        return new GenerarTurnosResponse(desde, hasta, generados, omitidos, creados);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TurnoResponse> listarPorRango(LocalDate desde, LocalDate hasta) {
        return turnoRepository.findByFechaBetweenOrderByFechaAscUsuarioUsuarioIdAsc(desde, hasta).stream()
                .map(turnoMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TurnoResponse> listarPorUsuario(Integer usuarioId, LocalDate desde, LocalDate hasta) {
        return turnoRepository.findByUsuarioUsuarioIdAndFechaBetweenOrderByFechaAsc(usuarioId, desde, hasta).stream()
                .map(turnoMapper::toResponse)
                .toList();
    }

    @Override
    public TurnoResponse actualizar(Integer turnoId, ActualizarTurnoRequest request) {
        Turno turno = obtenerOFallar(turnoId);

        if (request.horarioId() != null) {
            Horario horario = horarioRepository.findById(request.horarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Horario no encontrado: " + request.horarioId()));
            turno.setHorario(horario);
            // Al cambiar de turno base, las horas por defecto siguen al nuevo horario
            turno.setHoraInicio(horario.getHoraEntrada());
            turno.setHoraFin(horario.getHoraSalida());
        }
        if (request.horaInicio() != null) {
            turno.setHoraInicio(request.horaInicio());
        }
        if (request.horaFin() != null) {
            turno.setHoraFin(request.horaFin());
        }
        if (request.estado() != null) {
            turno.setEstado(request.estado());
        }
        // Toda edición manual marca el turno como excepción respecto a la plantilla
        turno.setOrigen(OrigenTurno.MANUAL);

        return turnoMapper.toResponse(turnoRepository.save(turno));
    }

    @Override
    public void cancelar(Integer turnoId) {
        Turno turno = obtenerOFallar(turnoId);
        turno.setEstado(EstadoTurno.CANCELADO);
        turno.setOrigen(OrigenTurno.MANUAL);
        turnoRepository.save(turno);
    }

    private Turno obtenerOFallar(Integer turnoId) {
        return turnoRepository.findById(turnoId)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado: " + turnoId));
    }
}
