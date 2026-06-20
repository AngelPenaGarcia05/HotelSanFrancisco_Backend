package com.sanfrancisco.api.modules.seguridad.exception;

import com.sanfrancisco.api.exception.BusinessException;

public class UsuarioInactivoException extends BusinessException {

    public UsuarioInactivoException(String message) {
        super(message);
    }
}
