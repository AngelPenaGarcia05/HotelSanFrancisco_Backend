package com.sanfrancisco.api.modules.recepcion.dto.request;

import com.sanfrancisco.api.modules.recepcion.enums.ModalidadPago;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record MisReservasCreateRequest(

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

        // Modalidad de pago: PARCIAL (50%) o TOTAL (100%). Si es null, el backend asume PARCIAL.
        ModalidadPago modalidadPago,

        @Size(max = 2000, message = "Las observaciones no pueden exceder 2000 caracteres")
        String observaciones,

        @NotEmpty(message = "La reserva debe incluir al menos una habitación")
        @Valid
        List<ReservaHabitacionRequest> habitaciones,

        // Opcional para el endpoint /mis-reservas: si llega vacío o nulo, el backend
        // infiere/crea el huésped principal a partir del usuario autenticado (JWT).
        @Valid
        List<HuespedReservaRequest> huespedes
) {
}
