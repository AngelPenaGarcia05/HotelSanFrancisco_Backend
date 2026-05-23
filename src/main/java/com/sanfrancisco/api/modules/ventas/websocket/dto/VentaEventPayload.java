package com.sanfrancisco.api.modules.ventas.websocket.dto;

import com.sanfrancisco.api.modules.ventas.enums.EstadoVenta;
import com.sanfrancisco.api.modules.ventas.enums.TipoVenta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payload minimalista para eventos en tiempo real de ventas.
 * Los clientes consumen este DTO en lugar del response completo para
 * reducir el ancho de banda en streams de alta frecuencia.
 */
public record VentaEventPayload(
        Integer ventaId,
        String codigoVenta,
        EstadoVenta estado,
        TipoVenta tipoVenta,
        BigDecimal montoTotal,
        LocalDateTime fechaVenta
) {
}
