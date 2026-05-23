package com.sanfrancisco.api.modules.rrhh.service.impl;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.rrhh.dto.request.AsignarHorarioRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.DetalleHorarioResponse;
import com.sanfrancisco.api.modules.rrhh.entity.DetalleHorario;
import com.sanfrancisco.api.modules.rrhh.entity.DetalleHorarioPK;
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
        DetalleHorarioPK id = new DetalleHorarioPK(request.usuarioId(), request.horarioId());
        
        if (detalleHorarioRepository.existsById(id)) {
            DetalleHorario existente = detalleHorarioRepository.findById(id).get();
            if (existente.getEstado() == EstadoActivo.ACTIVO) {
                throw new BusinessException("El usuario ya tiene asignado este horario");
            } else {
                existente.setEstado(EstadoActivo.ACTIVO);
                existente.setDiaSemana(request.diaSemana());
                existente.setFechaVigenciaInicio(request.fechaVigenciaInicio());
                existente.setFechaVigenciaFin(request.fechaVigenciaFin());
                return detalleHorarioMapper.toResponse(detalleHorarioRepository.save(existente));
            }
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
    public DetalleHorarioResponse update(Integer usuarioId, Integer horarioId, AsignarHorarioRequest request) {
        DetalleHorarioPK id = new DetalleHorarioPK(usuarioId, horarioId);
        DetalleHorario detalle = detalleHorarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación de horario no encontrada"));
                
        detalleHorarioMapper.updateEntity(detalle, request);
        return detalleHorarioMapper.toResponse(detalleHorarioRepository.save(detalle));
    }

    @Override
    public void remover(Integer usuarioId, Integer horarioId) {
        DetalleHorarioPK id = new DetalleHorarioPK(usuarioId, horarioId);
        DetalleHorario detalle = detalleHorarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación de horario no encontrada"));
        
        detalle.setEstado(EstadoActivo.INACTIVO);
        detalleHorarioRepository.save(detalle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DetalleHorarioResponse> findByUsuarioId(Integer usuarioId) {
        return detalleHorarioRepository.findByIdUsuarioId(usuarioId).stream()
                .map(detalleHorarioMapper::toResponse)
                .toList();
    }
}
