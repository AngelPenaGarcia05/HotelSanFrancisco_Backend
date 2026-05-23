package com.sanfrancisco.api.modules.compras.service.impl;

import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.compras.dto.request.CreateProveedorRequest;
import com.sanfrancisco.api.modules.compras.dto.request.ProveedorFilterRequest;
import com.sanfrancisco.api.modules.compras.dto.request.UpdateProveedorRequest;
import com.sanfrancisco.api.modules.compras.dto.response.ProveedorResponse;
import com.sanfrancisco.api.modules.compras.entity.Proveedor;
import com.sanfrancisco.api.modules.compras.mapper.ProveedorMapper;
import com.sanfrancisco.api.modules.compras.repository.ProveedorRepository;
import com.sanfrancisco.api.modules.compras.service.interfaces.ProveedorService;
import com.sanfrancisco.api.modules.compras.specification.ProveedorSpecification;
import com.sanfrancisco.api.shared.cache.CacheNames;
import com.sanfrancisco.api.shared.exception.ConflictException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Catálogo: datos poco cambiantes, alta lectura. Estrategia de cache:
 *  - findById  -> @Cacheable
 *  - create    -> @CachePut con el ID resultante
 *  - update    -> @CachePut (sobrescribe la entrada del ID)
 *  - delete    -> @CacheEvict
 * Las consultas paginadas no se cachean (claves variables).
 */
@Service
@Transactional
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository repository;
    private final ProveedorMapper mapper;

    public ProveedorServiceImpl(ProveedorRepository repository, ProveedorMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @CachePut(value = CacheNames.PROVEEDORES, key = "#result.proveedorId()")
    public ProveedorResponse create(CreateProveedorRequest request) {
        if (request.rucNitCif() != null && !request.rucNitCif().isBlank()
                && repository.existsByRucNitCif(request.rucNitCif())) {
            throw new ConflictException("Ya existe un proveedor con RUC/NIT/CIF: " + request.rucNitCif());
        }
        Proveedor saved = repository.save(mapper.toEntity(request));
        return mapper.toResponse(saved);
    }

    @Override
    @CachePut(value = CacheNames.PROVEEDORES, key = "#proveedorId")
    public ProveedorResponse update(Integer proveedorId, UpdateProveedorRequest request) {
        Proveedor proveedor = obtenerOFallar(proveedorId);

        if (request.rucNitCif() != null && !request.rucNitCif().equalsIgnoreCase(proveedor.getRucNitCif())
                && repository.existsByRucNitCif(request.rucNitCif())) {
            throw new ConflictException("Ya existe un proveedor con RUC/NIT/CIF: " + request.rucNitCif());
        }

        mapper.updateEntity(proveedor, request);
        return mapper.toResponse(repository.save(proveedor));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.PROVEEDORES, key = "#proveedorId")
    public ProveedorResponse findById(Integer proveedorId) {
        return mapper.toResponse(obtenerOFallar(proveedorId));
    }

    @Override
    @Transactional(readOnly = true)
    public ProveedorResponse findByRuc(String rucNitCif) {
        Proveedor proveedor = repository.findByRucNitCif(rucNitCif)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con RUC: " + rucNitCif));
        return mapper.toResponse(proveedor);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProveedorResponse> search(ProveedorFilterRequest filter, Pageable pageable) {
        return repository.findAll(ProveedorSpecification.build(filter), pageable)
                .map(mapper::toResponse);
    }

    @Override
    @CacheEvict(value = CacheNames.PROVEEDORES, key = "#proveedorId")
    public void deleteById(Integer proveedorId) {
        if (!repository.existsById(proveedorId)) {
            throw new ResourceNotFoundException("Proveedor no encontrado: " + proveedorId);
        }
        repository.deleteById(proveedorId);
    }

    private Proveedor obtenerOFallar(Integer proveedorId) {
        return repository.findById(proveedorId)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado: " + proveedorId));
    }
}
