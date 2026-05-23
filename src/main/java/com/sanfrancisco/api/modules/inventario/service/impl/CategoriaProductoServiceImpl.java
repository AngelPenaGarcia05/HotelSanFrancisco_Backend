package com.sanfrancisco.api.modules.inventario.service.impl;

import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.inventario.dto.request.CreateCategoriaProductoRequest;
import com.sanfrancisco.api.modules.inventario.dto.request.UpdateCategoriaProductoRequest;
import com.sanfrancisco.api.modules.inventario.dto.response.CategoriaProductoResponse;
import com.sanfrancisco.api.modules.inventario.entity.CategoriaProducto;
import com.sanfrancisco.api.modules.inventario.mapper.CategoriaProductoMapper;
import com.sanfrancisco.api.modules.inventario.repository.CategoriaProductoRepository;
import com.sanfrancisco.api.modules.inventario.service.interfaces.CategoriaProductoService;
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
public class CategoriaProductoServiceImpl implements CategoriaProductoService {

    private final CategoriaProductoRepository repository;
    private final CategoriaProductoMapper mapper;

    public CategoriaProductoServiceImpl(CategoriaProductoRepository repository, CategoriaProductoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @CachePut(value = CacheNames.CATEGORIAS_PRODUCTO, key = "#result.categoriaProductoId()")
    public CategoriaProductoResponse create(CreateCategoriaProductoRequest request) {
        if (repository.existsByNombre(request.nombre())) {
            throw new ConflictException("Ya existe una categoría con nombre: " + request.nombre());
        }
        CategoriaProducto saved = repository.save(mapper.toEntity(request));
        return mapper.toResponse(saved);
    }

    @Override
    @CachePut(value = CacheNames.CATEGORIAS_PRODUCTO, key = "#id")
    public CategoriaProductoResponse update(Integer id, UpdateCategoriaProductoRequest request) {
        CategoriaProducto entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CategoriaProducto no encontrada: " + id));

        if (request.nombre() != null && !request.nombre().equalsIgnoreCase(entity.getNombre())
                && repository.existsByNombre(request.nombre())) {
            throw new ConflictException("Ya existe una categoría con nombre: " + request.nombre());
        }

        mapper.updateEntity(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.CATEGORIAS_PRODUCTO, key = "#id")
    public CategoriaProductoResponse findById(Integer id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("CategoriaProducto no encontrada: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoriaProductoResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaProductoResponse> findByEstado(EstadoActivo estado) {
        return repository.findByEstado(estado).stream().map(mapper::toResponse).toList();
    }

    @Override
    @CacheEvict(value = CacheNames.CATEGORIAS_PRODUCTO, key = "#id")
    public void deleteById(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("CategoriaProducto no encontrada: " + id);
        }
        repository.deleteById(id);
    }
}
