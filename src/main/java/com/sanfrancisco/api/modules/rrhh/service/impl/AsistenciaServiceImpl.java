package com.sanfrancisco.api.modules.rrhh.service.impl;

import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.rrhh.dto.request.AsistenciaFilterRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.CreateAsistenciaRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.UpdateAsistenciaRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.AsistenciaResponse;
import com.sanfrancisco.api.modules.rrhh.entity.Asistencia;
import com.sanfrancisco.api.modules.rrhh.mapper.AsistenciaMapper;
import com.sanfrancisco.api.modules.rrhh.repository.AsistenciaRepository;
import com.sanfrancisco.api.modules.rrhh.service.interfaces.AsistenciaService;
import com.sanfrancisco.api.modules.rrhh.specification.AsistenciaSpecification;
import com.sanfrancisco.api.modules.rrhh.websocket.AsistenciaEventPublisher;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AsistenciaServiceImpl implements AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AsistenciaMapper asistenciaMapper;
    private final AsistenciaEventPublisher eventPublisher;

    public AsistenciaServiceImpl(AsistenciaRepository asistenciaRepository,
                                 UsuarioRepository usuarioRepository,
                                 AsistenciaMapper asistenciaMapper,
                                 AsistenciaEventPublisher eventPublisher) {
        this.asistenciaRepository = asistenciaRepository;
        this.usuarioRepository = usuarioRepository;
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
    }

    private Asistencia obtenerOFallar(Integer asistenciaId) {
        return asistenciaRepository.findById(asistenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Asistencia no encontrada: " + asistenciaId));
    }
}
