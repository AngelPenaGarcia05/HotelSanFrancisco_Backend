package com.sanfrancisco.api.modules.servicios.dto.request;

import jakarta.validation.constraints.*;

/**
 * Datos para que el cliente pida un servicio durante su estadía. La estancia se
 * infiere del usuario autenticado (estancia activa = check-in hecho, sin check-out).
 */
public record CreatePedidoServicioRequest(

        @NotNull(message = "El servicio es obligatorio")
        Integer tipoServicioId,

        // La cantidad es siempre un número entero de unidades. El @Max(99) es un tope
        // duro de seguridad; el tope de negocio real lo aplica el service (default 50,
        // o cantidadMaxima por tipo en una fase posterior).
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        @Max(value = 99, message = "La cantidad solicitada es demasiado alta")
        Integer cantidad,

        @Size(max = 2000, message = "Las observaciones no pueden exceder 2000 caracteres")
        String observaciones
) {
}
