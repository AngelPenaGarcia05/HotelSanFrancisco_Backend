package com.sanfrancisco.api.modules.operaciones.dto.request;

import com.sanfrancisco.api.modules.operaciones.enums.PrioridadIncidencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateIncidenciaRequest(

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 2000, message = "La descripción no puede exceder 2000 caracteres")
        String descripcion,

        LocalDateTime fechaReporte,

        @NotNull(message = "La prioridad es obligatoria")
        PrioridadIncidencia prioridad,

        @NotNull(message = "El usuario es obligatorio")
        Integer usuarioId,

        Integer reservaHabitacionId
) {
}
