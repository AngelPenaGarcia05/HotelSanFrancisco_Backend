package com.sanfrancisco.api.modules.booking.dto;

import com.sanfrancisco.api.modules.pagos.enums.TipoPago;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BookingConfirmationResponse(
        Integer reservaId,
        String codReserva,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Integer noches,

        // Primera habitación (compatibilidad con vistas de una sola habitación).
        String habitacionNumero,
        Integer habitacionPiso,
        String tipoHabitacionNombre,

        // Todas las habitaciones de la reserva (selección múltiple).
        List<HabitacionReservadaResumen> habitaciones,

        String huespedNombres,
        String huespedApellidos,
        String huespedDocumento,
        String huespedCorreo,
        String huespedTelefono,

        BigDecimal precioNoche,
        BigDecimal subtotal,
        BigDecimal impuesto,
        BigDecimal montoTotal,

        TipoPago tipoPago,
        BigDecimal adelanto,
        BigDecimal montoPendiente,

        // PENDIENTE al crear la pre-reserva; CONFIRMADA tras autorizar el pago.
        EstadoReserva estadoReserva
) {}
