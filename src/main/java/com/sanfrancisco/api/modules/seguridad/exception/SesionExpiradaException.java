package com.sanfrancisco.api.modules.seguridad.exception;

import com.sanfrancisco.api.exception.BusinessException;

public class SesionExpiradaException extends BusinessException {

    public SesionExpiradaException(String message) {
        super(message);
    }
}
