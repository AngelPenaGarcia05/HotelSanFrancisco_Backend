package com.sanfrancisco.api.modules.recepcion.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MisReservasCreateRequest(

        @NotBlank(message = "El código de reserva es obligatorio")
        @Size(max = 30, message = "El código de reserva no puede exceder 30 caracteres")
        @Pattern(regexp = "^[A-Z0-9\\-]+$", message = "El código solo permite mayúsculas, números y guiones")
        String codReserva,

        @NotNull(message = "La fecha de inicio es obligatoria")
        @FutureOrPresent(message = "La fecha de inicio no puede ser anterior a hoy")
        LocalDate fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria")
        @Future(message = "La fecha de fin debe ser posterior a hoy")
        LocalDate fechaFin,

        @NotNull(message = "El número de adultos es obligatorio")
        @Min(value = 1, message = "Debe haber al menos un adulto")
        Integer nroAdultos,

        @NotNull(message = "El número de niños es obligatorio")
        @Min(value = 0, message = "El número de niños no puede ser negativo")
        Integer nroNinos,

        @PositiveOrZero(message = "El adelanto no puede ser negativo")
        BigDecimal adelanto,

        @Size(max = 2000, message = "Las observaciones no pueden exceder 2000 caracteres")
        String observaciones,

        @NotEmpty(message = "La reserva debe incluir al menos una habitación")
        @Valid
        List<ReservaHabitacionRequest> habitaciones,

        @NotEmpty(message = "La reserva debe incluir al menos un huésped")
        @Valid
        List<HuespedReservaRequest> huespedes
) {
}
