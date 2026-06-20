package com.sanfrancisco.api.modules.recepcion.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record ReservaOnlineRequest(

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
