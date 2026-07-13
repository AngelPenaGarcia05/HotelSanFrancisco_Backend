package com.sanfrancisco.api.modules.operaciones.dto.response;

/**
 * Opción seleccionable para asociar una incidencia a una habitación ocupada.
 * El frontend usa {@code reservaHabitacionId} como valor del selector y
 * "Hab. {numeroHabitacion} — {codReserva}" como etiqueta visible.
 */
public record HabitacionIncidenciaResponse(
        Integer reservaHabitacionId,
        Integer habitacionId,
        String numeroHabitacion,
        Integer piso,
        String codReserva
) {
}
