package com.sanfrancisco.api.modules.recepcion.dto.request;

import com.sanfrancisco.api.modules.recepcion.enums.EstadoHabitacion;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateHabitacionRequest(

        @NotBlank @Size(max = 10)
        String numero,

        @NotNull @Min(1)
        Integer piso,

        @NotNull
        EstadoHabitacion estado,

        String descripcion,

        String observaciones
) {}
