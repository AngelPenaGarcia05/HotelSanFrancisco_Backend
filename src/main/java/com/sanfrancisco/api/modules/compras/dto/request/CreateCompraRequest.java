package com.sanfrancisco.api.modules.compras.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateCompraRequest(

        @NotNull(message = "El proveedor es obligatorio")
        Integer proveedorId,

        @NotNull(message = "La fecha de compra es obligatoria")
        @PastOrPresent(message = "La fecha de compra no puede ser futura")
        LocalDate fechaCompra,

        @Size(max = 50, message = "El número de factura no puede exceder 50 caracteres")
        String numeroFactura,

        @NotNull(message = "El impuesto es obligatorio")
        @PositiveOrZero(message = "El impuesto no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "Formato de impuesto inválido")
        BigDecimal impuesto,

        @NotEmpty(message = "La compra debe tener al menos un detalle")
        @Valid
        List<CreateDetalleCompraRequest> detalles
) {
}
