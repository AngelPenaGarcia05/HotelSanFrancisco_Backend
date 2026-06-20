package com.sanfrancisco.api.modules.compras.mapper;

import com.sanfrancisco.api.modules.compras.dto.request.CreateDetalleCompraRequest;
import com.sanfrancisco.api.modules.compras.dto.response.DetalleCompraResponse;
import com.sanfrancisco.api.modules.compras.entity.Compra;
import com.sanfrancisco.api.modules.compras.entity.DetalleCompra;
import com.sanfrancisco.api.modules.compras.entity.DetalleCompraPK;
import com.sanfrancisco.api.modules.inventario.entity.Producto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DetalleCompraMapper {

    public DetalleCompra toEntity(CreateDetalleCompraRequest request, Compra compra, Producto producto) {
        BigDecimal subtotal = request.cantidad().multiply(request.costoUnitario());
        DetalleCompraPK pk = new DetalleCompraPK(compra.getCompraId(), producto.getProductoId());
        return DetalleCompra.builder()
                .id(pk)
                .compra(compra)
                .producto(producto)
                .cantidad(request.cantidad())
                .costoUnitario(request.costoUnitario())
                .subtotal(subtotal)
                .build();
    }

    public DetalleCompraResponse toResponse(DetalleCompra entity) {
        Producto p = entity.getProducto();
        return new DetalleCompraResponse(
                entity.getId() != null ? entity.getId().getCompraId() : null,
                p != null ? p.getProductoId() : null,
                p != null ? p.getNombre() : null,
                entity.getCantidad(),
                entity.getCostoUnitario(),
                entity.getSubtotal()
        );
    }
}
