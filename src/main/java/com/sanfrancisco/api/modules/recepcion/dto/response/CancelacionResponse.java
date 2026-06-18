package com.sanfrancisco.api.modules.recepcion.dto.response;

import java.math.BigDecimal;

public record CancelacionResponse(
        ReservaResponse reserva,
        BigDecimal adelantoPagado,
        BigDecimal penalizacion,
        BigDecimal montoDevolucion,
        String politicaAplicada
) {
}
