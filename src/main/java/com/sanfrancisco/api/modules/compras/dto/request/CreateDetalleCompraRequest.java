package com.sanfrancisco.api.modules.compras.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateDetalleCompraRequest(

        @NotNull(message = "El producto es obligatorio")
        Integer productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a cero")
        @Digits(integer = 8, fraction = 2, message = "Formato de cantidad inválido")
        BigDecimal cantidad,

        @NotNull(message = "El costo unitario es obligatorio")
        @PositiveOrZero(message = "El costo unitario no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "Formato de costo inválido")
        BigDecimal costoUnitario
) {
}
