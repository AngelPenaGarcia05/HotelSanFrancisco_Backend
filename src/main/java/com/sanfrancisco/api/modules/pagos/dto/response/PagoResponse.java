package com.sanfrancisco.api.modules.pagos.dto.response;

import com.sanfrancisco.api.modules.pagos.enums.TipoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoResponse(
        Integer pagoId,
        Integer metodoPagoId,
        String metodoPagoNombre,
        TipoPago tipoPago,
        LocalDateTime fecha,
        BigDecimal monto,
        String comprobante,
        Integer ventaId,
        Integer reservaId,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {
}
