package com.sanfrancisco.api.modules.recepcion.dto.response;

import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ReservaResponse(
        Integer reservaId,
        String codReserva,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        BigDecimal montoTotal,
        EstadoReserva estado,
        Integer nroAdultos,
        Integer nroNinos,
        BigDecimal subtotal,
        BigDecimal descuento,
        BigDecimal adelanto,
        BigDecimal impuesto,
        String observaciones,
        Integer usuarioId,
        String usuarioNombre,
        Integer canalId,
        String canalNombre,
        Integer estanciaId,
        List<ReservaHabitacionResponse> habitaciones,
        List<DetalleHuespedResponse> huespedes,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion,
        boolean llegadaHoy
) {
}
