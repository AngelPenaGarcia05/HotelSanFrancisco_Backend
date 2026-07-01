package com.sanfrancisco.api.modules.servicios.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServicioResponse(
        Integer servicioId,
        Integer tipoServicioId,
        String tipoServicioNombre,
        Integer estanciaId,
        Integer cantidad,
        BigDecimal precioAplicado,
        BigDecimal subtotal,
        String observaciones,
        LocalDateTime fechaConsumo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {
}
