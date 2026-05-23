package com.sanfrancisco.api.modules.pagos.dto.response;

import com.sanfrancisco.api.shared.enums.EstadoActivo;

import java.time.LocalDateTime;

public record MetodoPagoResponse(
        Integer metodoPagoId,
        String nombre,
        EstadoActivo estado,
        Boolean requiereComprobante,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {
}
