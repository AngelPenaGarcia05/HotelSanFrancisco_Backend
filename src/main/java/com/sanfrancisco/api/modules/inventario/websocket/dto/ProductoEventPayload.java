package com.sanfrancisco.api.modules.inventario.websocket.dto;

import com.sanfrancisco.api.shared.enums.EstadoActivo;

import java.math.BigDecimal;

/**
 * Payload minimalista para eventos en tiempo real de productos / stock.
 * Los clientes consumen este DTO en lugar del response completo para
 * reducir el ancho de banda en streams de alta frecuencia.
 */
public record ProductoEventPayload(
        Integer productoId,
        String nombre,
        BigDecimal stockActual,
        BigDecimal stockMinimo,
        EstadoActivo estado
) {
}
