package com.sanfrancisco.api.modules.ventas.mapper;

import com.sanfrancisco.api.modules.inventario.entity.Producto;
import com.sanfrancisco.api.modules.ventas.dto.request.CreateDetalleVentaRequest;
import com.sanfrancisco.api.modules.ventas.dto.response.DetalleVentaResponse;
import com.sanfrancisco.api.modules.ventas.entity.DetalleVenta;
import com.sanfrancisco.api.modules.ventas.entity.DetalleVentaPK;
import com.sanfrancisco.api.modules.ventas.entity.Venta;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class DetalleVentaMapper {

    public DetalleVenta toEntity(CreateDetalleVentaRequest request, Venta venta, Producto producto) {
        BigDecimal descuento = Optional.ofNullable(request.descuentoUnitario()).orElse(BigDecimal.ZERO);
        // Precio autoritativo: el del catálogo, no el que envía el cliente del API
        BigDecimal precioCatalogo = producto.getPrecioVenta();
        BigDecimal precioNeto = precioCatalogo.subtract(descuento);
        BigDecimal subtotal = precioNeto.multiply(request.cantidad());
        DetalleVentaPK pk = new DetalleVentaPK(venta.getVentaId(), producto.getProductoId());
        return DetalleVenta.builder()
                .id(pk)
                .venta(venta)
                .producto(producto)
                .cantidad(request.cantidad())
                .precioUnitario(precioCatalogo)
                .descuentoUnitario(descuento)
                .subtotal(subtotal)
                .build();
    }

    public DetalleVentaResponse toResponse(DetalleVenta entity) {
        Producto p = entity.getProducto();
        return new DetalleVentaResponse(
                entity.getId() != null ? entity.getId().getVentaId() : null,
                p != null ? p.getProductoId() : null,
                p != null ? p.getNombre() : null,
                entity.getCantidad(),
                entity.getPrecioUnitario(),
                entity.getDescuentoUnitario(),
                entity.getSubtotal()
        );
    }
}
