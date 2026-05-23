package com.sanfrancisco.api.shared.websocket;

import java.time.Instant;

/**
 * Envoltorio estándar para todos los eventos publicados vía WebSocket.
 * Mantiene una forma consistente en el cliente independientemente del payload.
 */
public record WebSocketEvent<T>(
        String type,
        String entity,
        T payload,
        Instant timestamp
) {

    public static <T> WebSocketEvent<T> of(String type, String entity, T payload) {
        return new WebSocketEvent<>(type, entity, payload, Instant.now());
    }
}
