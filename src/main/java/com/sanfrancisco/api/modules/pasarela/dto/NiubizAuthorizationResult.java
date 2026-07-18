package com.sanfrancisco.api.modules.pasarela.dto;

/**
 * Resultado normalizado de la autorización en Niubiz.
 * {@code aprobado} = ACTION_CODE "000". El resto de campos vienen del
 * dataMap de la respuesta; {@code respuestaRaw} conserva el JSON completo
 * para auditoría y reconciliación.
 */
public record NiubizAuthorizationResult(
        boolean aprobado,
        String codigoAccion,
        String descripcion,
        String codigoAutorizacion,
        String tarjetaEnmascarada,
        String marcaTarjeta,
        String transactionId,
        String respuestaRaw
) {}
