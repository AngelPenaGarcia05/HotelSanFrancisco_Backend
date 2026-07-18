package com.sanfrancisco.api.modules.pasarela.client;

/**
 * Fallo de comunicación o de configuración con Niubiz (no un rechazo de la
 * transacción: los rechazos se modelan en {@code NiubizAuthorizationResult}).
 */
public class NiubizClientException extends RuntimeException {

    public NiubizClientException(String message) {
        super(message);
    }

    public NiubizClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
