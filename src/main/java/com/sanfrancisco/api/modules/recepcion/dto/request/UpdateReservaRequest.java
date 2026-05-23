package com.sanfrancisco.api.modules.recepcion.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Update parcial: todos los campos son opcionales; solo se aplican los presentes (no-null).
 * Las reglas de transición de estado se manejan vía endpoints/operaciones dedicadas, no aquí.
 */
public record UpdateReservaRequest(

        LocalDate fechaInicio,

        LocalDate fechaFin,

        @Min(value = 1, message = "Debe haber al menos un adulto en la reserva")
        Integer nroAdultos,

        @Min(value = 0, message = "El número de niños no puede ser negativo")
        Integer nroNinos,

        @PositiveOrZero(message = "El subtotal no puede ser negativo")
        BigDecimal subtotal,

        @PositiveOrZero(message = "El descuento no puede ser negativo")
        BigDecimal descuento,

        @PositiveOrZero(message = "El adelanto no puede ser negativo")
        BigDecimal adelanto,

        @PositiveOrZero(message = "El impuesto no puede ser negativo")
        BigDecimal impuesto,

        @Size(max = 2000, message = "Las observaciones no pueden exceder 2000 caracteres")
        String observaciones,

        Integer canalId
) {
}
