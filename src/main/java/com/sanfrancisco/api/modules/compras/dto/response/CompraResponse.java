package com.sanfrancisco.api.modules.compras.dto.response;

import com.sanfrancisco.api.modules.compras.enums.EstadoCompra;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CompraResponse(
        Integer compraId,
        Integer proveedorId,
        String proveedorRazonSocial,
        LocalDate fechaCompra,
        String numeroFactura,
        BigDecimal subtotal,
        BigDecimal impuesto,
        BigDecimal montoTotal,
        EstadoCompra estado,
        List<DetalleCompraResponse> detalles,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {
}
