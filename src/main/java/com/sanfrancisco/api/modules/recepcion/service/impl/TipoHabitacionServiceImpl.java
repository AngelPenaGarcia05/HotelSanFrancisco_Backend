package com.sanfrancisco.api.modules.recepcion.service.impl;

import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.recepcion.dto.request.CreateTipoHabitacionRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.UpdateTipoHabitacionRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.TipoHabitacionResponse;
import com.sanfrancisco.api.modules.recepcion.entity.TipoHabitacion;
import com.sanfrancisco.api.modules.recepcion.mapper.TipoHabitacionMapper;
import com.sanfrancisco.api.modules.recepcion.repository.TipoHabitacionRepository;
import com.sanfrancisco.api.modules.recepcion.service.interfaces.TipoHabitacionService;
import com.sanfrancisco.api.shared.cache.CacheNames;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Catálogo: datos poco cambiantes, alta lectura. Estrategia de cache:
 *  - findById  -> @Cacheable
 *  - create    -> @CachePut con el ID resultante
 *  - update    -> @CachePut (sobrescribe la entrada del ID)
 *  - delete    -> @CacheEvict
 * Las consultas paginadas y por estado NO se cachean (claves variables).
 */
@Service
@Transactional
public class TipoHabitacionServiceImpl implements TipoHabitacionService {

    private final TipoHabitacionRepository repository;
    private final TipoHabitacionMapper mapper;

    public TipoHabitacionServiceImpl(TipoHabitacionRepository repository, TipoHabitacionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @CachePut(value = CacheNames.TIPOS_HABITACION, key = "#result.tipoHabitacionId()")
    public TipoHabitacionResponse create(CreateTipoHabitacionRequest request) {
        TipoHabitacion saved = repository.save(mapper.toEntity(request));
        return mapper.toResponse(saved);
    }

    @Override
    @CachePut(value = CacheNames.TIPOS_HABITACION, key = "#id")
    public TipoHabitacionResponse update(Integer id, UpdateTipoHabitacionRequest request) {
        TipoHabitacion entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TipoHabitacion no encontrado: " + id));
        mapper.updateEntity(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.TIPOS_HABITACION, key = "#id")
    public TipoHabitacionResponse findById(Integer id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("TipoHabitacion no encontrado: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TipoHabitacionResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoHabitacionResponse> findByEstado(EstadoActivo estado) {
        return repository.findByEstado(estado).stream().map(mapper::toResponse).toList();
    }

    @Override
    @CacheEvict(value = CacheNames.TIPOS_HABITACION, key = "#id")
    public void deleteById(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("TipoHabitacion no encontrado: " + id);
        }
        repository.deleteById(id);
    }
}
