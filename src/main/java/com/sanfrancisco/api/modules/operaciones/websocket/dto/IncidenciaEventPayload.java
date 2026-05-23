package com.sanfrancisco.api.modules.operaciones.websocket.dto;

import com.sanfrancisco.api.modules.operaciones.enums.EstadoIncidencia;
import com.sanfrancisco.api.modules.operaciones.enums.PrioridadIncidencia;

import java.time.LocalDateTime;

/**
 * Payload minimalista para eventos en tiempo real de incidencias.
 * Los clientes consumen este DTO en lugar del response completo para
 * reducir el ancho de banda en streams de alta frecuencia.
 */
public record IncidenciaEventPayload(
        Integer incidenciaId,
        EstadoIncidencia estado,
        PrioridadIncidencia prioridad,
        LocalDateTime fechaReporte,
        Integer usuarioId,
        Integer reservaHabitacionId
) {
}
