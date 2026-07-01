package com.sanfrancisco.api.modules.servicios.dto.response;

import com.sanfrancisco.api.shared.enums.EstadoActivo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TipoServicioResponse(
        Integer tipoServicioId,
        String nombre,
        BigDecimal costoBase,
        String descripcion,
        Integer cantidadMaxima,
        EstadoActivo estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {
}
