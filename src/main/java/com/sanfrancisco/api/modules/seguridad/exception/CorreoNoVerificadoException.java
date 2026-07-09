package com.sanfrancisco.api.modules.seguridad.exception;

import com.sanfrancisco.api.exception.BusinessException;

/**
 * Se lanza al intentar iniciar sesión con una cuenta cuyo correo aún no ha sido
 * verificado. El frontend debe ofrecer reenviar el código de verificación.
 */
public class CorreoNoVerificadoException extends BusinessException {

    public CorreoNoVerificadoException(String message) {
        super(message);
    }
}
