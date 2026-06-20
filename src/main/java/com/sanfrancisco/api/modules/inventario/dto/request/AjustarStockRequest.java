package com.sanfrancisco.api.modules.inventario.dto.request;

import com.sanfrancisco.api.modules.inventario.enums.TipoAjusteStock;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record AjustarStockRequest(

        @NotNull(message = "El tipo de ajuste es obligatorio")
        TipoAjusteStock tipoAjuste,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a cero")
        @Digits(integer = 8, fraction = 2, message = "Formato de cantidad inválido")
        BigDecimal cantidad,

        @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
        String motivo
) {
}
