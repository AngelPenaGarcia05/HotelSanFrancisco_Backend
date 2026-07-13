package com.sanfrancisco.api.modules.ventas.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateDetalleVentaRequest(

        @NotNull(message = "El producto es obligatorio")
        Integer productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a cero")
        @Digits(integer = 8, fraction = 2, message = "Formato de cantidad inválido")
        BigDecimal cantidad,

        // Opcional e ignorado por el servidor: el precio autoritativo es el del
        // catálogo (producto.precioVenta), no el que envíe el cliente. Se acepta
        // por compatibilidad con clientes que aún lo mandan; si viene, solo se
        // valida su formato.
        @PositiveOrZero(message = "El precio unitario no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "Formato de precio inválido")
        BigDecimal precioUnitario,

        @PositiveOrZero(message = "El descuento unitario no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "Formato de descuento inválido")
        BigDecimal descuentoUnitario
) {
}
