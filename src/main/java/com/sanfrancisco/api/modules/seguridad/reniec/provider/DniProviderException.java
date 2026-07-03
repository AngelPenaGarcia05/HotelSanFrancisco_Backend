package com.sanfrancisco.api.modules.seguridad.reniec.provider;

/**
 * Falla técnica de un proveedor de DNI (timeout, token inválido, HTTP 5xx).
 * Distinta de "DNI no encontrado", que se modela como {@code Optional.empty()}.
 */
public class DniProviderException extends RuntimeException {

    public DniProviderException(String message) {
        super(message);
    }

    public DniProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
