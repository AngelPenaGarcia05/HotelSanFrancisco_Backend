package com.sanfrancisco.api.modules.inventario.service.interfaces;

import com.sanfrancisco.api.modules.inventario.dto.request.CreateCategoriaProductoRequest;
import com.sanfrancisco.api.modules.inventario.dto.request.UpdateCategoriaProductoRequest;
import com.sanfrancisco.api.modules.inventario.dto.response.CategoriaProductoResponse;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoriaProductoService {

    CategoriaProductoResponse create(CreateCategoriaProductoRequest request);

    CategoriaProductoResponse update(Integer id, UpdateCategoriaProductoRequest request);

    CategoriaProductoResponse findById(Integer id);

    Page<CategoriaProductoResponse> findAll(Pageable pageable);

    List<CategoriaProductoResponse> findByEstado(EstadoActivo estado);

    void deleteById(Integer id);
}
