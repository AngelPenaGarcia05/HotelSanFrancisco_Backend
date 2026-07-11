package com.sanfrancisco.api.modules.rrhh.dto.response;

import java.time.LocalDate;
import java.util.List;

/**
 * Resumen de la generación de turnos desde la plantilla.
 * generados = turnos nuevos creados; omitidos = fechas que ya tenían turno.
 */
public record GenerarTurnosResponse(
        LocalDate desde,
        LocalDate hasta,
        int generados,
        int omitidos,
        List<TurnoResponse> turnos
) {
}
