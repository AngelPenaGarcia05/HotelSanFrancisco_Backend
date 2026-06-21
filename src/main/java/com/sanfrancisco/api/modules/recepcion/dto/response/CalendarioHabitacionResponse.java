package com.sanfrancisco.api.modules.recepcion.dto.response;

import com.sanfrancisco.api.modules.recepcion.enums.EstadoHabitacion;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CalendarioHabitacionResponse(
        Integer habitacionId,
        String numero,
        Integer piso,
        String tipoHabitacionNombre,
        BigDecimal precioBase,
        EstadoHabitacion estado,
        List<CalendarioReservaItem> reservas
) {

    public record CalendarioReservaItem(
            Integer reservaId,
            String codReserva,
            String huesped,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            EstadoReserva estadoReserva
    ) {}
}
