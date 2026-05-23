package com.sanfrancisco.api.modules.recepcion.dto.request;

import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Filtros opcionales para búsqueda paginada/dinámica de reservas.
 * Cualquier campo null se ignora en la specification resultante.
 */
public record ReservaFilterRequest(
        String codReserva,
        EstadoReserva estado,
        Integer usuarioId,
        Integer canalId,
        LocalDate fechaInicioDesde,
        LocalDate fechaInicioHasta,
        LocalDate fechaFinDesde,
        LocalDate fechaFinHasta,
        BigDecimal montoTotalMin,
        BigDecimal montoTotalMax
) {
}
