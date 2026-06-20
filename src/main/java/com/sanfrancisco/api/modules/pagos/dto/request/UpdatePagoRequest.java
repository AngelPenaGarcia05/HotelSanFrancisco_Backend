package com.sanfrancisco.api.modules.pagos.dto.request;

import com.sanfrancisco.api.modules.pagos.enums.TipoPago;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Update parcial: todos los campos son opcionales; solo se aplican los presentes (no-null).
 * Los vínculos a venta/reserva no se modifican vía update; deben gestionarse al crear el pago.
 */
public record UpdatePagoRequest(

        Integer metodoPagoId,

        TipoPago tipoPago,

        LocalDateTime fecha,

        @PositiveOrZero(message = "El monto no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "Formato de monto inválido")
        BigDecimal monto,

        @Size(max = 100, message = "El comprobante no puede exceder 100 caracteres")
        String comprobante
) {
}
