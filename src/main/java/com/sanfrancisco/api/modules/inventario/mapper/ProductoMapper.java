package com.sanfrancisco.api.modules.inventario.mapper;

import com.sanfrancisco.api.modules.inventario.dto.request.CreateProductoRequest;
import com.sanfrancisco.api.modules.inventario.dto.request.UpdateProductoRequest;
import com.sanfrancisco.api.modules.inventario.dto.response.ProductoResponse;
import com.sanfrancisco.api.modules.inventario.entity.CategoriaProducto;
import com.sanfrancisco.api.modules.inventario.entity.Producto;
import org.springframework.stereotype.Component;

@Component
public class ProductoMapper {

    public Producto toEntity(CreateProductoRequest request, CategoriaProducto categoria) {
        return Producto.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .precioVenta(request.precioVenta())
                .stockActual(request.stockActual())
                .stockMinimo(request.stockMinimo())
                .estado(request.estado())
                .categoriaProducto(categoria)
                .build();
    }

    public void updateEntity(Producto target, UpdateProductoRequest request, CategoriaProducto categoria) {
        if (request.nombre() != null) target.setNombre(request.nombre());
        if (request.descripcion() != null) target.setDescripcion(request.descripcion());
        if (request.precioVenta() != null) target.setPrecioVenta(request.precioVenta());
        if (request.stockMinimo() != null) target.setStockMinimo(request.stockMinimo());
        if (request.estado() != null) target.setEstado(request.estado());
        if (categoria != null) target.setCategoriaProducto(categoria);
    }

    public ProductoResponse toResponse(Producto entity) {
        CategoriaProducto c = entity.getCategoriaProducto();
        return new ProductoResponse(
                entity.getProductoId(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getPrecioVenta(),
                entity.getStockActual(),
                entity.getStockMinimo(),
                entity.getEstado(),
                c != null ? c.getCategoriaProductoId() : null,
                c != null ? c.getNombre() : null,
                entity.getFechaCreacion(),
                entity.getFechaModificacion()
        );
    }
}
