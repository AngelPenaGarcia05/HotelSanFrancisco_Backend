package com.sanfrancisco.api.modules.pasarela.dto;

/** Resultado de crear una sesión de checkout en Niubiz. */
public record NiubizSessionData(
        String sessionKey,
        Long expirationTime
) {}
