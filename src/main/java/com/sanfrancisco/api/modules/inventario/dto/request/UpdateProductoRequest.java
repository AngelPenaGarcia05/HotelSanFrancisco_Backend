package com.sanfrancisco.api.modules.inventario.dto.request;

import com.sanfrancisco.api.shared.enums.EstadoActivo;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Update parcial: todos los campos son opcionales; solo se aplican los presentes (no-null).
 * El stock actual NO se modifica vía update; debe usarse el endpoint dedicado de ajuste.
 */
public record UpdateProductoRequest(

        @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
        String nombre,

        @Size(max = 2000, message = "La descripción no puede exceder 2000 caracteres")
        String descripcion,

        @PositiveOrZero(message = "El precio de venta no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "Formato de precio inválido")
        BigDecimal precioVenta,

        @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
        @Digits(integer = 8, fraction = 2, message = "Formato de stock inválido")
        BigDecimal stockMinimo,

        EstadoActivo estado,

        Integer categoriaProductoId
) {
}
