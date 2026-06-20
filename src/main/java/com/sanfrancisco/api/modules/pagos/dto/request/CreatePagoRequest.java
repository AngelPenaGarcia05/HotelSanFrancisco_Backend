package com.sanfrancisco.api.modules.pagos.dto.request;

import com.sanfrancisco.api.modules.pagos.enums.TipoPago;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreatePagoRequest(

        @NotNull(message = "El método de pago es obligatorio")
        Integer metodoPagoId,

        @NotNull(message = "El tipo de pago es obligatorio")
        TipoPago tipoPago,

        LocalDateTime fecha,

        @NotNull(message = "El monto es obligatorio")
        @PositiveOrZero(message = "El monto no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "Formato de monto inválido")
        BigDecimal monto,

        @Size(max = 100, message = "El comprobante no puede exceder 100 caracteres")
        String comprobante,

        Integer ventaId,

        Integer reservaId
) {
}
