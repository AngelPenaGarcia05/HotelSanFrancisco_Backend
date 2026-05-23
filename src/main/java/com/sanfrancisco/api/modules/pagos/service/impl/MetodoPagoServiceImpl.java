package com.sanfrancisco.api.modules.pagos.service.impl;

import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.pagos.dto.request.CreateMetodoPagoRequest;
import com.sanfrancisco.api.modules.pagos.dto.request.UpdateMetodoPagoRequest;
import com.sanfrancisco.api.modules.pagos.dto.response.MetodoPagoResponse;
import com.sanfrancisco.api.modules.pagos.entity.MetodoPago;
import com.sanfrancisco.api.modules.pagos.mapper.MetodoPagoMapper;
import com.sanfrancisco.api.modules.pagos.repository.MetodoPagoRepository;
import com.sanfrancisco.api.modules.pagos.service.interfaces.MetodoPagoService;
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
public class MetodoPagoServiceImpl implements MetodoPagoService {

    private final MetodoPagoRepository repository;
    private final MetodoPagoMapper mapper;

    public MetodoPagoServiceImpl(MetodoPagoRepository repository, MetodoPagoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @CachePut(value = CacheNames.METODOS_PAGO, key = "#result.metodoPagoId()")
    public MetodoPagoResponse create(CreateMetodoPagoRequest request) {
        MetodoPago saved = repository.save(mapper.toEntity(request));
        return mapper.toResponse(saved);
    }

    @Override
    @CachePut(value = CacheNames.METODOS_PAGO, key = "#metodoPagoId")
    public MetodoPagoResponse update(Integer metodoPagoId, UpdateMetodoPagoRequest request) {
        MetodoPago entity = obtenerOFallar(metodoPagoId);
        mapper.updateEntity(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.METODOS_PAGO, key = "#metodoPagoId")
    public MetodoPagoResponse findById(Integer metodoPagoId) {
        return mapper.toResponse(obtenerOFallar(metodoPagoId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MetodoPagoResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetodoPagoResponse> findByEstado(EstadoActivo estado) {
        return repository.findByEstado(estado).stream().map(mapper::toResponse).toList();
    }

    @Override
    @CacheEvict(value = CacheNames.METODOS_PAGO, key = "#metodoPagoId")
    public void deleteById(Integer metodoPagoId) {
        if (!repository.existsById(metodoPagoId)) {
            throw new ResourceNotFoundException("MetodoPago no encontrado: " + metodoPagoId);
        }
        repository.deleteById(metodoPagoId);
    }

    private MetodoPago obtenerOFallar(Integer metodoPagoId) {
        return repository.findById(metodoPagoId)
                .orElseThrow(() -> new ResourceNotFoundException("MetodoPago no encontrado: " + metodoPagoId));
    }
}
