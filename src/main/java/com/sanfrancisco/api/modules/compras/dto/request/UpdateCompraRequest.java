package com.sanfrancisco.api.modules.compras.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Update parcial: todos los campos son opcionales; solo se aplican los presentes (no-null).
 * Los detalles no se modifican vía update; deben gestionarse en endpoints/operaciones dedicadas.
 */
public record UpdateCompraRequest(

        Integer proveedorId,

        @PastOrPresent(message = "La fecha de compra no puede ser futura")
        LocalDate fechaCompra,

        @Size(max = 50, message = "El número de factura no puede exceder 50 caracteres")
        String numeroFactura,

        @PositiveOrZero(message = "El impuesto no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "Formato de impuesto inválido")
        BigDecimal impuesto
) {
}
