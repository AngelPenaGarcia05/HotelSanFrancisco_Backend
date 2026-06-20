package com.sanfrancisco.api.modules.servicios.dto.request;

import com.sanfrancisco.api.shared.enums.EstadoActivo;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateTipoServicioRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String nombre,

        @NotNull(message = "El costo base es obligatorio")
        @PositiveOrZero(message = "El costo base no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "Formato de costo base inválido")
        BigDecimal costoBase,

        @Size(max = 2000, message = "La descripción no puede exceder 2000 caracteres")
        String descripcion,

        @NotNull(message = "El estado es obligatorio")
        EstadoActivo estado
) {
}
