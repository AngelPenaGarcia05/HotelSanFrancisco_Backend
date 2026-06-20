package com.sanfrancisco.api.modules.pagos.dto.request;

import com.sanfrancisco.api.shared.enums.EstadoActivo;
import jakarta.validation.constraints.Size;

public record UpdateMetodoPagoRequest(

        @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
        String nombre,

        EstadoActivo estado,

        Boolean requiereComprobante
) {
}
