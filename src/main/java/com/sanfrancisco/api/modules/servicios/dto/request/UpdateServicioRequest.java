package com.sanfrancisco.api.modules.servicios.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Update parcial: todos los campos son opcionales; solo se aplican los presentes (no-null).
 * Si cambia cantidad o precioAplicado el service recalcula el subtotal.
 */
public record UpdateServicioRequest(

        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        @Max(value = 99, message = "La cantidad es demasiado alta")
        Integer cantidad,

        @PositiveOrZero(message = "El precio aplicado no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "Formato de precio aplicado inválido")
        BigDecimal precioAplicado,

        @Size(max = 2000, message = "Las observaciones no pueden exceder 2000 caracteres")
        String observaciones,

        LocalDateTime fechaConsumo
) {
}
