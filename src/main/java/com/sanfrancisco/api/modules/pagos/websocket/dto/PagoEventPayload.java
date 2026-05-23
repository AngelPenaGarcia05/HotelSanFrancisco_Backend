package com.sanfrancisco.api.modules.pagos.websocket.dto;

import com.sanfrancisco.api.modules.pagos.enums.TipoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payload minimalista para eventos en tiempo real de pagos.
 * Los clientes consumen este DTO en lugar del response completo para
 * reducir el ancho de banda en streams de alta frecuencia.
 */
public record PagoEventPayload(
        Integer pagoId,
        TipoPago tipoPago,
        BigDecimal monto,
        LocalDateTime fecha,
        Integer metodoPagoId,
        Integer ventaId,
        Integer reservaId
) {
}
