package com.sanfrancisco.api.modules.pagos.dto.request;

import com.sanfrancisco.api.shared.enums.EstadoActivo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateMetodoPagoRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
        String nombre,

        @NotNull(message = "El estado es obligatorio")
        EstadoActivo estado,

        @NotNull(message = "El indicador de comprobante es obligatorio")
        Boolean requiereComprobante
) {
}
