package com.sanfrancisco.api.modules.ventas.dto.request;

import com.sanfrancisco.api.modules.ventas.enums.EstadoVenta;
import com.sanfrancisco.api.modules.ventas.enums.TipoVenta;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Filtros opcionales para búsqueda paginada/dinámica de ventas.
 * Cualquier campo null se ignora en la specification resultante.
 * Los rangos de fecha son días de calendario; los límites horarios
 * del día los fija el servidor (ver SpecificationUtils.dateTimeInDayRange).
 */
public record VentaFilterRequest(
        String codigoVenta,
        EstadoVenta estado,
        TipoVenta tipoVenta,
        Integer usuarioId,
        Integer estanciaId,
        Integer huespedId,
        LocalDate fechaVentaDesde,
        LocalDate fechaVentaHasta,
        BigDecimal montoTotalMin,
        BigDecimal montoTotalMax
) {
}
