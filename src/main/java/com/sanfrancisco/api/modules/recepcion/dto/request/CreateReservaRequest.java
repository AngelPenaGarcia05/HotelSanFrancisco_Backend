package com.sanfrancisco.api.modules.recepcion.dto.request;

import com.sanfrancisco.api.modules.recepcion.enums.ModalidadPago;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateReservaRequest(

        // El código lo genera el backend; si llega del front se ignora.
        @Size(max = 30, message = "El código de reserva no puede exceder 30 caracteres")
        @Pattern(regexp = "^[A-Z0-9\\-]*$", message = "El código de reserva solo permite mayúsculas, números y guiones")
        String codReserva,

        @NotNull(message = "La fecha de inicio es obligatoria")
        @FutureOrPresent(message = "La fecha de inicio no puede ser anterior a hoy")
        LocalDate fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria")
        @Future(message = "La fecha de fin debe ser posterior a hoy")
        LocalDate fechaFin,

        @NotNull(message = "El número de adultos es obligatorio")
        @Min(value = 1, message = "Debe haber al menos un adulto en la reserva")
        Integer nroAdultos,

        @NotNull(message = "El número de niños es obligatorio")
        @Min(value = 0, message = "El número de niños no puede ser negativo")
        Integer nroNinos,

        // descuento: solo aplicable por staff y con tope (30% del subtotal); cliente => 0.
        @PositiveOrZero(message = "El descuento no puede ser negativo")
        BigDecimal descuento,

        // adelanto e impuesto se IGNORAN: el backend los recalcula. Se mantienen
        // en el contrato solo por compatibilidad hacia atrás con el front actual.
        @PositiveOrZero(message = "El adelanto no puede ser negativo")
        BigDecimal adelanto,

        @PositiveOrZero(message = "El impuesto no puede ser negativo")
        BigDecimal impuesto,

        // Modalidad de pago: PARCIAL (50%) o TOTAL (100%). Si es null, el backend asume PARCIAL.
        ModalidadPago modalidadPago,

        @Size(max = 2000, message = "Las observaciones no pueden exceder 2000 caracteres")
        String observaciones,

        @NotNull(message = "El usuario es obligatorio")
        Integer usuarioId,

        Integer canalId,

        @NotEmpty(message = "La reserva debe incluir al menos una habitación")
        @Valid
        List<ReservaHabitacionRequest> habitaciones,

        @NotEmpty(message = "La reserva debe incluir al menos un huésped")
        @Valid
        List<HuespedReservaRequest> huespedes,

        // Si es true, ignora la advertencia de posible duplicado y crea la reserva igualmente
        Boolean forzar
) {
}
