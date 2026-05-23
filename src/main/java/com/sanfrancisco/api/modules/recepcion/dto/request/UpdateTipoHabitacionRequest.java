package com.sanfrancisco.api.modules.recepcion.dto.request;

import com.sanfrancisco.api.shared.enums.EstadoActivo;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpdateTipoHabitacionRequest(

        @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
        String nombre,

        @PositiveOrZero(message = "El precio base no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "Formato de precio inválido")
        BigDecimal precioBase,

        @Size(max = 2000, message = "La descripción no puede exceder 2000 caracteres")
        String descripcion,

        EstadoActivo estado,

        @Min(value = 1, message = "La capacidad máxima debe ser al menos 1")
        @Max(value = 20, message = "La capacidad máxima no puede exceder 20")
        Integer capacidadMaxima
) {
}
