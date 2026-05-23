package com.sanfrancisco.api.modules.seguridad.service.impl;

import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.seguridad.dto.request.CreateTipoDocumentoRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.UpdateTipoDocumentoRequest;
import com.sanfrancisco.api.modules.seguridad.dto.response.TipoDocumentoResponse;
import com.sanfrancisco.api.modules.seguridad.entity.TipoDocumento;
import com.sanfrancisco.api.modules.seguridad.mapper.TipoDocumentoMapper;
import com.sanfrancisco.api.modules.seguridad.repository.TipoDocumentoRepository;
import com.sanfrancisco.api.modules.seguridad.service.interfaces.TipoDocumentoService;
import com.sanfrancisco.api.shared.cache.CacheNames;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import com.sanfrancisco.api.shared.exception.ConflictException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TipoDocumentoServiceImpl implements TipoDocumentoService {

    private final TipoDocumentoRepository repository;
    private final TipoDocumentoMapper mapper;

    public TipoDocumentoServiceImpl(TipoDocumentoRepository repository, TipoDocumentoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @CachePut(value = CacheNames.TIPOS_DOCUMENTO, key = "#result.tipoDocumentoId()")
    public TipoDocumentoResponse create(CreateTipoDocumentoRequest request) {
        if (repository.existsByAcronimo(request.acronimo())) {
            throw new ConflictException("Ya existe un tipo de documento con el acrónimo: " + request.acronimo());
        }
        TipoDocumento saved = repository.save(mapper.toEntity(request));
        return mapper.toResponse(saved);
    }

    @Override
    @CachePut(value = CacheNames.TIPOS_DOCUMENTO, key = "#id")
    public TipoDocumentoResponse update(Integer id, UpdateTipoDocumentoRequest request) {
        TipoDocumento entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TipoDocumento no encontrado: " + id));

        if (request.acronimo() != null && !request.acronimo().equalsIgnoreCase(entity.getAcronimo())
                && repository.existsByAcronimo(request.acronimo())) {
            throw new ConflictException("Ya existe un tipo de documento con el acrónimo: " + request.acronimo());
        }

        mapper.updateEntity(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.TIPOS_DOCUMENTO, key = "#id")
    public TipoDocumentoResponse findById(Integer id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("TipoDocumento no encontrado: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TipoDocumentoResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoDocumentoResponse> findByEstado(EstadoActivo estado) {
        return repository.findByEstado(estado).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @CacheEvict(value = CacheNames.TIPOS_DOCUMENTO, key = "#id")
    public void deleteById(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("TipoDocumento no encontrado: " + id);
        }
        repository.deleteById(id);
    }
}
