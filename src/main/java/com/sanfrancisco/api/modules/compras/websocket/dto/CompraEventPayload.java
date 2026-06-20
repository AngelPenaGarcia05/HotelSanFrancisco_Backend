package com.sanfrancisco.api.modules.compras.websocket.dto;

import com.sanfrancisco.api.modules.compras.enums.EstadoCompra;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload minimalista para eventos en tiempo real de compras.
 * Los clientes consumen este DTO en lugar del response completo para
 * reducir el ancho de banda en streams de alta frecuencia.
 */
public record CompraEventPayload(
        Integer compraId,
        String numeroFactura,
        EstadoCompra estado,
        LocalDate fechaCompra,
        BigDecimal montoTotal,
        Integer proveedorId
) {
}
