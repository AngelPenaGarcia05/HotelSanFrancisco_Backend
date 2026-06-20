package com.sanfrancisco.api.modules.inventario.mapper;

import com.sanfrancisco.api.modules.inventario.dto.request.CreateCategoriaProductoRequest;
import com.sanfrancisco.api.modules.inventario.dto.request.UpdateCategoriaProductoRequest;
import com.sanfrancisco.api.modules.inventario.dto.response.CategoriaProductoResponse;
import com.sanfrancisco.api.modules.inventario.entity.CategoriaProducto;
import org.springframework.stereotype.Component;

@Component
public class CategoriaProductoMapper {

    public CategoriaProducto toEntity(CreateCategoriaProductoRequest request) {
        return CategoriaProducto.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .estado(request.estado())
                .build();
    }

    public void updateEntity(CategoriaProducto target, UpdateCategoriaProductoRequest request) {
        if (request.nombre() != null) target.setNombre(request.nombre());
        if (request.descripcion() != null) target.setDescripcion(request.descripcion());
        if (request.estado() != null) target.setEstado(request.estado());
    }

    public CategoriaProductoResponse toResponse(CategoriaProducto entity) {
        return new CategoriaProductoResponse(
                entity.getCategoriaProductoId(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getEstado(),
                entity.getFechaCreacion(),
                entity.getFechaModificacion()
        );
    }
}
