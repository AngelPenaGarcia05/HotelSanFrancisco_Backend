package com.sanfrancisco.api.modules.ventas.dto.response;

import java.math.BigDecimal;

public record DetalleVentaResponse(
        Integer ventaId,
        Integer productoId,
        String productoNombre,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        BigDecimal descuentoUnitario,
        BigDecimal subtotal
) {
}
