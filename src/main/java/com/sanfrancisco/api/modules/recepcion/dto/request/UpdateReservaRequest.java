package com.sanfrancisco.api.modules.recepcion.dto.request;

import com.sanfrancisco.api.modules.recepcion.enums.ModalidadPago;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Update parcial: todos los campos son opcionales; solo se aplican los presentes (no-null).
 * Si se provee la lista de habitaciones, se reemplaza completamente la lista existente.
 * Las transiciones de estado se manejan vía endpoints dedicados, no aquí.
 */
public record UpdateReservaRequest(

        LocalDate fechaInicio,

        LocalDate fechaFin,

        @Min(value = 1, message = "Debe haber al menos un adulto en la reserva")
        Integer nroAdultos,

        @Min(value = 0, message = "El número de niños no puede ser negativo")
        Integer nroNinos,

        // descuento: editable por staff, con tope del 30% del subtotal.
        @PositiveOrZero(message = "El descuento no puede ser negativo")
        BigDecimal descuento,

        // adelanto e impuesto se IGNORAN: el backend los recalcula. Se conservan
        // por compatibilidad hacia atrás con el front actual.
        @PositiveOrZero(message = "El adelanto no puede ser negativo")
        BigDecimal adelanto,

        @PositiveOrZero(message = "El impuesto no puede ser negativo")
        BigDecimal impuesto,

        // Modalidad de pago: PARCIAL (50%) o TOTAL (100%). Si es null, se conserva la actual.
        ModalidadPago modalidadPago,

        @Size(max = 2000, message = "Las observaciones no pueden exceder 2000 caracteres")
        String observaciones,

        Integer canalId,

        @Valid
        List<ReservaHabitacionRequest> habitaciones,

        @Valid
        List<HuespedReservaRequest> huespedes
) {
}
