package com.sanfrancisco.api.exception;

/**
 * El usuario está autenticado pero no tiene permiso para operar sobre el recurso
 * solicitado (p. ej. una reserva que no le pertenece). Se traduce a HTTP 403.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
