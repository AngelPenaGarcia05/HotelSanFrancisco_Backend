package com.sanfrancisco.api.modules.servicios.service.impl;

import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.servicios.dto.request.CreateTipoServicioRequest;
import com.sanfrancisco.api.modules.servicios.dto.request.UpdateTipoServicioRequest;
import com.sanfrancisco.api.modules.servicios.dto.response.TipoServicioResponse;
import com.sanfrancisco.api.modules.servicios.entity.TipoServicio;
import com.sanfrancisco.api.modules.servicios.mapper.TipoServicioMapper;
import com.sanfrancisco.api.modules.servicios.repository.TipoServicioRepository;
import com.sanfrancisco.api.modules.servicios.service.interfaces.TipoServicioService;
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
public class TipoServicioServiceImpl implements TipoServicioService {

    private final TipoServicioRepository repository;
    private final TipoServicioMapper mapper;

    public TipoServicioServiceImpl(TipoServicioRepository repository, TipoServicioMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @CachePut(value = CacheNames.TIPOS_SERVICIO, key = "#result.tipoServicioId()")
    public TipoServicioResponse create(CreateTipoServicioRequest request) {
        TipoServicio saved = repository.save(mapper.toEntity(request));
        return mapper.toResponse(saved);
    }

    @Override
    @CachePut(value = CacheNames.TIPOS_SERVICIO, key = "#tipoServicioId")
    public TipoServicioResponse update(Integer tipoServicioId, UpdateTipoServicioRequest request) {
        TipoServicio entity = obtenerOFallar(tipoServicioId);
        mapper.updateEntity(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.TIPOS_SERVICIO, key = "#tipoServicioId")
    public TipoServicioResponse findById(Integer tipoServicioId) {
        return mapper.toResponse(obtenerOFallar(tipoServicioId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TipoServicioResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoServicioResponse> findByEstado(EstadoActivo estado) {
        return repository.findByEstado(estado).stream().map(mapper::toResponse).toList();
    }

    @Override
    @CacheEvict(value = CacheNames.TIPOS_SERVICIO, key = "#tipoServicioId")
    public void deleteById(Integer tipoServicioId) {
        if (!repository.existsById(tipoServicioId)) {
            throw new ResourceNotFoundException("TipoServicio no encontrado: " + tipoServicioId);
        }
        repository.deleteById(tipoServicioId);
    }

    private TipoServicio obtenerOFallar(Integer tipoServicioId) {
        return repository.findById(tipoServicioId)
                .orElseThrow(() -> new ResourceNotFoundException("TipoServicio no encontrado: " + tipoServicioId));
    }
}
