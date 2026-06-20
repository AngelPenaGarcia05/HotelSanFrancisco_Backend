package com.sanfrancisco.api.modules.servicios.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Update parcial: todos los campos son opcionales; solo se aplican los presentes (no-null).
 * Si cambia cantidad o precioAplicado el service recalcula el subtotal.
 */
public record UpdateServicioRequest(

        @Positive(message = "La cantidad debe ser mayor a cero")
        @Digits(integer = 8, fraction = 2, message = "Formato de cantidad inválido")
        BigDecimal cantidad,

        @PositiveOrZero(message = "El precio aplicado no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "Formato de precio aplicado inválido")
        BigDecimal precioAplicado,

        @Size(max = 2000, message = "Las observaciones no pueden exceder 2000 caracteres")
        String observaciones,

        LocalDateTime fechaConsumo
) {
}
