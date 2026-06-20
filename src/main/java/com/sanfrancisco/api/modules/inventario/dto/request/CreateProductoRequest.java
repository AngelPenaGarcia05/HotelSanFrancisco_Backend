package com.sanfrancisco.api.modules.inventario.dto.request;

import com.sanfrancisco.api.shared.enums.EstadoActivo;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateProductoRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
        String nombre,

        @Size(max = 2000, message = "La descripción no puede exceder 2000 caracteres")
        String descripcion,

        @NotNull(message = "El precio de venta es obligatorio")
        @PositiveOrZero(message = "El precio de venta no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "Formato de precio inválido")
        BigDecimal precioVenta,

        @NotNull(message = "El stock actual es obligatorio")
        @PositiveOrZero(message = "El stock actual no puede ser negativo")
        @Digits(integer = 8, fraction = 2, message = "Formato de stock inválido")
        BigDecimal stockActual,

        @NotNull(message = "El stock mínimo es obligatorio")
        @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
        @Digits(integer = 8, fraction = 2, message = "Formato de stock inválido")
        BigDecimal stockMinimo,

        @NotNull(message = "El estado es obligatorio")
        EstadoActivo estado,

        @NotNull(message = "La categoría es obligatoria")
        Integer categoriaProductoId
) {
}
