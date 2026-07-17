package com.sanfrancisco.api.modules.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConfirmarPagoRequest(

        @NotBlank(message = "El número de compra es obligatorio")
        @Size(max = 12)
        String purchaseNumber,

        @NotBlank(message = "El token de transacción es obligatorio")
        @Size(max = 200)
        String transactionToken
) {}
