package com.sanfrancisco.api.modules.servicios.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateServicioRequest(

        @NotNull(message = "El tipo de servicio es obligatorio")
        Integer tipoServicioId,

        @NotNull(message = "La estancia es obligatoria")
        Integer estanciaId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a cero")
        @Digits(integer = 8, fraction = 2, message = "Formato de cantidad inválido")
        BigDecimal cantidad,

        @PositiveOrZero(message = "El precio aplicado no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "Formato de precio aplicado inválido")
        BigDecimal precioAplicado,

        @Size(max = 2000, message = "Las observaciones no pueden exceder 2000 caracteres")
        String observaciones,

        LocalDateTime fechaConsumo
) {
}
