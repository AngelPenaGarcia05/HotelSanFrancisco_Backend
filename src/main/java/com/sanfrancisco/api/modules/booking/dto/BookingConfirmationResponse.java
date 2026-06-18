package com.sanfrancisco.api.modules.booking.dto;

import com.sanfrancisco.api.modules.pagos.enums.TipoPago;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingConfirmationResponse(
        Integer reservaId,
        String codReserva,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Integer noches,

        String habitacionNumero,
        Integer habitacionPiso,
        String tipoHabitacionNombre,

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

        String metodoPagoNombre
) {}
