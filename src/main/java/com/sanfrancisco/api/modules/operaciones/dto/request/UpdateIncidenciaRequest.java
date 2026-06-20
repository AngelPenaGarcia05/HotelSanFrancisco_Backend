package com.sanfrancisco.api.modules.operaciones.dto.request;

import com.sanfrancisco.api.modules.operaciones.enums.PrioridadIncidencia;
import jakarta.validation.constraints.Size;

/**
 * Update parcial: todos los campos son opcionales; solo se aplican los presentes (no-null).
 * El estado no se modifica vía update; usar el endpoint dedicado de cambio de estado.
 */
public record UpdateIncidenciaRequest(

        @Size(max = 2000, message = "La descripción no puede exceder 2000 caracteres")
        String descripcion,

        PrioridadIncidencia prioridad,

        Integer reservaHabitacionId
) {
}
