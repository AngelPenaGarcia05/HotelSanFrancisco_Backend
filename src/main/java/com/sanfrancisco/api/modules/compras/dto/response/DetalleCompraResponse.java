package com.sanfrancisco.api.modules.compras.dto.response;

import java.math.BigDecimal;

public record DetalleCompraResponse(
        Integer compraId,
        Integer productoId,
        String productoNombre,
        BigDecimal cantidad,
        BigDecimal costoUnitario,
        BigDecimal subtotal
) {
}
