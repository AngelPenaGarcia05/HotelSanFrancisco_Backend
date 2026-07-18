package com.sanfrancisco.api.modules.booking.dto;

import java.math.BigDecimal;

/** Resumen de cada habitación incluida en la confirmación del booking público. */
public record HabitacionReservadaResumen(
        String numero,
        Integer piso,
        String tipoNombre,
        BigDecimal precioNoche
) {}
