package com.sanfrancisco.api.modules.servicios.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Filtros opcionales para búsqueda paginada/dinámica de servicios consumidos.
 * Cualquier campo null se ignora en la specification resultante.
 */
public record ServicioFilterRequest(
        Integer tipoServicioId,
        Integer estanciaId,
        LocalDateTime fechaConsumoDesde,
        LocalDateTime fechaConsumoHasta,
        BigDecimal subtotalMin,
        BigDecimal subtotalMax
) {
}
