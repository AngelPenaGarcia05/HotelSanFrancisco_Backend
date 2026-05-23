package com.sanfrancisco.api.modules.rrhh.service.impl;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.rrhh.dto.request.CreateHorarioRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.UpdateHorarioRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.HorarioResponse;
import com.sanfrancisco.api.modules.rrhh.entity.Horario;
import com.sanfrancisco.api.modules.rrhh.mapper.HorarioMapper;
import com.sanfrancisco.api.modules.rrhh.repository.HorarioRepository;
import com.sanfrancisco.api.modules.rrhh.service.interfaces.HorarioService;
import com.sanfrancisco.api.modules.rrhh.specification.HorarioSpecification;
import com.sanfrancisco.api.shared.cache.CacheNames;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import com.sanfrancisco.api.shared.exception.ConflictException;
import com.sanfrancisco.api.shared.exception.ValidationException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class HorarioServiceImpl implements HorarioService {

    private final HorarioRepository horarioRepository;
    private final HorarioMapper horarioMapper;

    public HorarioServiceImpl(HorarioRepository horarioRepository, HorarioMapper horarioMapper) {
        this.horarioRepository = horarioRepository;
        this.horarioMapper = horarioMapper;
    }

    @Override
    @CacheEvict(value = CacheNames.HORARIOS, allEntries = true)
    public HorarioResponse create(CreateHorarioRequest request) {
        validarFechas(request);

        if (horarioRepository.existsByNombreTurnoIgnoreCase(request.nombreTurno())) {
            throw new ConflictException("Ya existe un horario con el nombre: " + request.nombreTurno());
        }

        Horario entity = horarioMapper.toEntity(request);
        Horario saved = horarioRepository.save(entity);
        return horarioMapper.toResponse(saved);
    }

    @Override
    @CachePut(value = CacheNames.HORARIOS, key = "#horarioId")
    public HorarioResponse update(Integer horarioId, UpdateHorarioRequest request) {
        Horario horario = obtenerOFallar(horarioId);

        if (request.nombreTurno() != null && !request.nombreTurno().equalsIgnoreCase(horario.getNombreTurno())) {
            if (horarioRepository.existsByNombreTurnoIgnoreCase(request.nombreTurno())) {
                throw new ConflictException("Ya existe un horario con el nombre: " + request.nombreTurno());
            }
        }

        horarioMapper.updateEntity(horario, request);
        validarFechas(horario);

        Horario saved = horarioRepository.save(horario);
        return horarioMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.HORARIOS, key = "#horarioId")
    public HorarioResponse findById(Integer horarioId) {
        return horarioMapper.toResponse(obtenerOFallar(horarioId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HorarioResponse> search(String nombreTurno, EstadoActivo estado, Pageable pageable) {
        Specification<Horario> spec = Specification.where(HorarioSpecification.hasEstado(estado))
                .and(HorarioSpecification.hasNombreTurno(nombreTurno));
        return horarioRepository.findAll(spec, pageable).map(horarioMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HorarioResponse> findByEstado(EstadoActivo estado) {
        return horarioRepository.findByEstado(estado).stream()
                .map(horarioMapper::toResponse)
                .toList();
    }

    @Override
    @CacheEvict(value = CacheNames.HORARIOS, key = "#horarioId")
    public void deleteById(Integer horarioId) {
        Horario horario = obtenerOFallar(horarioId);
        
        // Logical delete or physical delete based on pattern. We'll do logical for Horario.
        if (horario.getEstado() == EstadoActivo.INACTIVO) {
            throw new BusinessException("El horario ya se encuentra inactivo");
        }
        horario.setEstado(EstadoActivo.INACTIVO);
        horarioRepository.save(horario);
    }

    private Horario obtenerOFallar(Integer horarioId) {
        return horarioRepository.findById(horarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Horario no encontrado: " + horarioId));
    }

    private void validarFechas(CreateHorarioRequest request) {
        if (request.horaEntrada() == null || request.horaSalida() == null) return;
        if (request.horaEntrada().equals(request.horaSalida())) {
            throw new ValidationException("La hora de entrada y salida no pueden ser iguales");
        }
    }
    
    private void validarFechas(Horario horario) {
        if (horario.getHoraEntrada() == null || horario.getHoraSalida() == null) return;
        if (horario.getHoraEntrada().equals(horario.getHoraSalida())) {
            throw new ValidationException("La hora de entrada y salida no pueden ser iguales");
        }
    }
}
