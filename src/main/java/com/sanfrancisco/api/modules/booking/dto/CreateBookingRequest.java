package com.sanfrancisco.api.modules.booking.dto;

import com.sanfrancisco.api.modules.pagos.enums.TipoPago;
import com.sanfrancisco.api.modules.recepcion.dto.request.AcompananteRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record CreateBookingRequest(

        @NotNull(message = "La fecha de entrada es obligatoria")
        LocalDate fechaInicio,

        @NotNull(message = "La fecha de salida es obligatoria")
        LocalDate fechaFin,

        // Compatibilidad: selección de una sola habitación. Si se envía
        // habitacionesIds este campo se ignora.
        Integer habitacionId,

        Integer tipoHabitacionId,

        /**
         * Selección múltiple de habitaciones. Solo se admite más de una cuando
         * hay 2+ adultos; el service valida la regla y la capacidad total.
         */
        List<Integer> habitacionesIds,

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

        /**
         * Acompañantes del huésped titular (huéspedes sin cuenta). Titular +
         * acompañantes no puede exceder adultos + niños.
         */
        @Valid
        List<AcompananteRequest> acompanantes,

        // El flujo público solo admite pago online: TOTAL = 100 %, ANTICIPO = 50 %.
        // El pago presencial/efectivo es exclusivo del módulo de recepción.
        @NotNull(message = "El tipo de pago es obligatorio")
        TipoPago tipoPago
) {}
