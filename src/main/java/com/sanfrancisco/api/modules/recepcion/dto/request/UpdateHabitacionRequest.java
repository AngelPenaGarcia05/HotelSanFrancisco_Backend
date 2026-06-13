package com.sanfrancisco.api.modules.recepcion.dto.request;

import com.sanfrancisco.api.modules.recepcion.enums.EstadoHabitacion;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateHabitacionRequest(

        @Size(max = 10)
        String numero,

        @Min(1)
        Integer piso,

        EstadoHabitacion estado,

        String descripcion,

        String observaciones
) {}
