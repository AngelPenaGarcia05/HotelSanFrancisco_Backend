package com.sanfrancisco.api.modules.compras.mapper;

import com.sanfrancisco.api.modules.compras.dto.request.CreateCompraRequest;
import com.sanfrancisco.api.modules.compras.dto.request.UpdateCompraRequest;
import com.sanfrancisco.api.modules.compras.dto.response.CompraResponse;
import com.sanfrancisco.api.modules.compras.dto.response.DetalleCompraResponse;
import com.sanfrancisco.api.modules.compras.entity.Compra;
import com.sanfrancisco.api.modules.compras.entity.Proveedor;
import com.sanfrancisco.api.modules.compras.enums.EstadoCompra;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Mapper manual. Calcula monto_total = subtotal + impuesto. El estado inicial al crear
 * es PENDIENTE; las transiciones de estado se gestionan en el service vía operaciones
 * dedicadas. El subtotal se calcula a partir de los detalles en el service.
 */
@Component
public class CompraMapper {

    public Compra toEntity(CreateCompraRequest request, Proveedor proveedor, BigDecimal subtotal) {
        BigDecimal impuesto = Optional.ofNullable(request.impuesto()).orElse(BigDecimal.ZERO);
        BigDecimal montoTotal = subtotal.add(impuesto);
        return Compra.builder()
                .proveedor(proveedor)
                .fechaCompra(request.fechaCompra())
                .numeroFactura(request.numeroFactura())
                .subtotal(subtotal)
                .impuesto(impuesto)
                .montoTotal(montoTotal)
                .estado(EstadoCompra.PENDIENTE)
                .build();
    }

    public void updateEntity(Compra target, UpdateCompraRequest request, Proveedor proveedor) {
        if (request.fechaCompra() != null) target.setFechaCompra(request.fechaCompra());
        if (request.numeroFactura() != null) target.setNumeroFactura(request.numeroFactura());
        if (request.impuesto() != null) {
            target.setImpuesto(request.impuesto());
            target.setMontoTotal(target.getSubtotal().add(request.impuesto()));
        }
        if (proveedor != null) target.setProveedor(proveedor);
    }

    public CompraResponse toResponse(Compra entity, List<DetalleCompraResponse> detalles) {
        Proveedor p = entity.getProveedor();
        return new CompraResponse(
                entity.getCompraId(),
                p != null ? p.getProveedorId() : null,
                p != null ? p.getRazonSocial() : null,
                entity.getFechaCompra(),
                entity.getNumeroFactura(),
                entity.getSubtotal(),
                entity.getImpuesto(),
                entity.getMontoTotal(),
                entity.getEstado(),
                detalles,
                entity.getFechaCreacion(),
                entity.getFechaModificacion()
        );
    }
}
