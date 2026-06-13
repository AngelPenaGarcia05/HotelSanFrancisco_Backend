package com.sanfrancisco.api.modules.recepcion.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CheckOutLiquidacionResponse(
        Integer reservaId,
        String codReserva,
        BigDecimal subtotal,
        BigDecimal descuento,
        BigDecimal impuesto,
        BigDecimal consumosAdicionales,
        BigDecimal montoTotal,
        BigDecimal adelanto,
        BigDecimal montoPendiente,
        LocalDateTime fechaCheckin,
        LocalDateTime fechaCheckout,
        Integer noches
) {}
