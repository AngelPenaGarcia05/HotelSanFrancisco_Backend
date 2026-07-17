package com.sanfrancisco.api.modules.booking.dto;

import java.math.BigDecimal;

/**
 * Datos que necesita el frontend para abrir el checkout de Niubiz.
 * El monto es el calculado por el backend; el checkout y la autorización
 * usan exactamente este valor (nunca uno enviado por el navegador).
 */
public record CrearSesionPagoResponse(
        Integer reservaId,
        String purchaseNumber,
        String sessionKey,
        String merchantId,
        BigDecimal monto,
        String moneda,
        String checkoutScriptUrl,
        Long expirationTime
) {}
