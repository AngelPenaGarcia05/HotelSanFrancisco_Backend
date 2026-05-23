package com.sanfrancisco.api.modules.servicios.websocket.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payload minimalista para eventos en tiempo real de servicios consumidos.
 * Los clientes consumen este DTO en lugar del response completo para
 * reducir el ancho de banda en streams de alta frecuencia.
 */
public record ServicioEventPayload(
        Integer servicioId,
        Integer tipoServicioId,
        Integer estanciaId,
        BigDecimal cantidad,
        BigDecimal subtotal,
        LocalDateTime fechaConsumo
) {
}
