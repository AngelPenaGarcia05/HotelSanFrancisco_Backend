package com.sanfrancisco.api.modules.rrhh.service.impl;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.rrhh.dto.request.AsignarHorarioRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.DetalleHorarioResponse;
import com.sanfrancisco.api.modules.rrhh.entity.DetalleHorario;
import com.sanfrancisco.api.modules.rrhh.entity.Horario;
import com.sanfrancisco.api.modules.rrhh.mapper.DetalleHorarioMapper;
import com.sanfrancisco.api.modules.rrhh.repository.DetalleHorarioRepository;
import com.sanfrancisco.api.modules.rrhh.repository.HorarioRepository;
import com.sanfrancisco.api.modules.rrhh.service.interfaces.DetalleHorarioService;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DetalleHorarioServiceImpl implements DetalleHorarioService {

    private final DetalleHorarioRepository detalleHorarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final HorarioRepository horarioRepository;
    private final DetalleHorarioMapper detalleHorarioMapper;

    public DetalleHorarioServiceImpl(DetalleHorarioRepository detalleHorarioRepository,
                                     UsuarioRepository usuarioRepository,
                                     HorarioRepository horarioRepository,
                                     DetalleHorarioMapper detalleHorarioMapper) {
        this.detalleHorarioRepository = detalleHorarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.horarioRepository = horarioRepository;
        this.detalleHorarioMapper = detalleHorarioMapper;
    }

    @Override
    public DetalleHorarioResponse asignar(AsignarHorarioRequest request) {
        // Regla de negocio: un empleado hace como máximo un turno ACTIVO por día.
        if (detalleHorarioRepository.existsByUsuarioUsuarioIdAndDiaSemanaAndEstado(
                request.usuarioId(), request.diaSemana(), EstadoActivo.ACTIVO)) {
            throw new BusinessException("El empleado ya tiene un turno asignado ese día de la semana");
        }

        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + request.usuarioId()));

        Horario horario = horarioRepository.findById(request.horarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Horario no encontrado: " + request.horarioId()));

        DetalleHorario entity = detalleHorarioMapper.toEntity(request, usuario, horario);
        DetalleHorario saved = detalleHorarioRepository.save(entity);
        return detalleHorarioMapper.toResponse(saved);
    }

    @Override
    public DetalleHorarioResponse update(Integer detalleHorarioId, AsignarHorarioRequest request) {
        DetalleHorario detalle = obtenerOFallar(detalleHorarioId);

        // Si cambia el día, validar que no colisione con otro turno ACTIVO del mismo empleado.
        if (request.diaSemana() != null && !request.diaSemana().equals(detalle.getDiaSemana())
                && detalleHorarioRepository.existsByUsuarioUsuarioIdAndDiaSemanaAndEstado(
                        detalle.getUsuario().getUsuarioId(), request.diaSemana(), EstadoActivo.ACTIVO)) {
            throw new BusinessException("El empleado ya tiene un turno asignado ese día de la semana");
        }

        detalleHorarioMapper.updateEntity(detalle, request);
        return detalleHorarioMapper.toResponse(detalleHorarioRepository.save(detalle));
    }

    @Override
    public void remover(Integer detalleHorarioId) {
        DetalleHorario detalle = obtenerOFallar(detalleHorarioId);
        detalle.setEstado(EstadoActivo.INACTIVO);
        detalleHorarioRepository.save(detalle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DetalleHorarioResponse> findByUsuarioId(Integer usuarioId) {
        return detalleHorarioRepository.findByUsuarioUsuarioId(usuarioId).stream()
                .map(detalleHorarioMapper::toResponse)
                .toList();
    }

    private DetalleHorario obtenerOFallar(Integer detalleHorarioId) {
        return detalleHorarioRepository.findById(detalleHorarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación de horario no encontrada: " + detalleHorarioId));
    }
}
