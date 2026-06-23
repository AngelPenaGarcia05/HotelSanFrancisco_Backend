package com.sanfrancisco.api.modules.servicios.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Datos para que el cliente pida un servicio durante su estadía. La estancia se
 * infiere del usuario autenticado (estancia activa = check-in hecho, sin check-out).
 */
public record CreatePedidoServicioRequest(

        @NotNull(message = "El servicio es obligatorio")
        Integer tipoServicioId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a cero")
        @Digits(integer = 8, fraction = 2, message = "Formato de cantidad inválido")
        BigDecimal cantidad,

        @Size(max = 2000, message = "Las observaciones no pueden exceder 2000 caracteres")
        String observaciones
) {
}
