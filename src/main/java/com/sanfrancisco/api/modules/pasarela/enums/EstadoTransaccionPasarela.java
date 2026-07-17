package com.sanfrancisco.api.modules.pasarela.enums;

/**
 * Ciclo de vida de un intento de cobro en la pasarela.
 * CREADA → AUTORIZADA | RECHAZADA | ERROR | EXPIRADA.
 * ERROR = la autorización no obtuvo respuesta concluyente (timeout/caída):
 * requiere reconciliación manual contra el panel de Niubiz antes de reintentar.
 */
public enum EstadoTransaccionPasarela {
    CREADA,
    AUTORIZADA,
    RECHAZADA,
    ERROR,
    EXPIRADA
}
