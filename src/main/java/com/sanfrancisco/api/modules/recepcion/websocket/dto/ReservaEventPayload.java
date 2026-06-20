package com.sanfrancisco.api.modules.recepcion.websocket.dto;

import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload minimalista para eventos en tiempo real de reservas.
 * Los clientes consumen este DTO en lugar del response completo para
 * reducir el ancho de banda en streams de alta frecuencia.
 */
public record ReservaEventPayload(
        Integer reservaId,
        String codReserva,
        EstadoReserva estado,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        BigDecimal montoTotal
) {
}
