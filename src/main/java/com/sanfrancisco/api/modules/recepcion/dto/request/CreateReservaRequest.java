package com.sanfrancisco.api.modules.recepcion.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateReservaRequest(

        @NotBlank(message = "El código de reserva es obligatorio")
        @Size(max = 30, message = "El código de reserva no puede exceder 30 caracteres")
        @Pattern(regexp = "^[A-Z0-9\\-]+$", message = "El código de reserva solo permite mayúsculas, números y guiones")
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

        // descuento, adelanto e impuesto los provee el cliente; subtotal se calcula del lado servidor
        @NotNull(message = "El descuento es obligatorio")
        @PositiveOrZero(message = "El descuento no puede ser negativo")
        BigDecimal descuento,

        @NotNull(message = "El adelanto es obligatorio")
        @PositiveOrZero(message = "El adelanto no puede ser negativo")
        BigDecimal adelanto,

        @NotNull(message = "El impuesto es obligatorio")
        @PositiveOrZero(message = "El impuesto no puede ser negativo")
        BigDecimal impuesto,

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
