package com.sanfrancisco.api.modules.servicios.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Motivo del rechazo de un pedido de servicio por parte de recepción. */
public record RechazarPedidoServicioRequest(

        @NotBlank(message = "El motivo del rechazo es obligatorio")
        @Size(max = 2000, message = "El motivo no puede exceder 2000 caracteres")
        String motivo
) {
}
