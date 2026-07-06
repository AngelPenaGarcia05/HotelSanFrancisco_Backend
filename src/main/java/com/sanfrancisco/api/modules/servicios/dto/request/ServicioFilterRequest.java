package com.sanfrancisco.api.modules.servicios.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Filtros opcionales para búsqueda paginada/dinámica de servicios consumidos.
 * Cualquier campo null se ignora en la specification resultante.
 * Los rangos de fecha son días de calendario; los límites horarios
 * del día los fija el servidor (ver SpecificationUtils.dateTimeInDayRange).
 */
public record ServicioFilterRequest(
        Integer tipoServicioId,
        Integer estanciaId,
        LocalDate fechaConsumoDesde,
        LocalDate fechaConsumoHasta,
        BigDecimal subtotalMin,
        BigDecimal subtotalMax
) {
}
