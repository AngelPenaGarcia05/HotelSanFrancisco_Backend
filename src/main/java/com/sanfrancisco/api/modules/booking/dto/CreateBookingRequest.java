package com.sanfrancisco.api.modules.booking.dto;

import com.sanfrancisco.api.modules.pagos.enums.TipoPago;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CreateBookingRequest(

        @NotNull(message = "La fecha de entrada es obligatoria")
        LocalDate fechaInicio,

        @NotNull(message = "La fecha de salida es obligatoria")
        LocalDate fechaFin,

        @NotNull(message = "La habitación es obligatoria")
        Integer habitacionId,

        @NotNull(message = "El tipo de habitación es obligatorio")
        Integer tipoHabitacionId,

        @NotBlank(message = "El número de documento es obligatorio")
        @Size(max = 20)
        String numeroDocumento,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80)
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 80)
        String apellidos,

        @Size(max = 20)
        String telefono,

        @Email(message = "Correo inválido")
        @Size(max = 150)
        String correo,

        @NotNull
        @Min(1)
        Integer nroAdultos,

        @Min(0)
        Integer nroNinos,

        @Size(max = 2000)
        String serviciosAdicionales,

        @NotNull(message = "El tipo de pago es obligatorio")
        TipoPago tipoPago,

        @NotNull(message = "El método de pago es obligatorio")
        Integer metodoPagoId
) {}
