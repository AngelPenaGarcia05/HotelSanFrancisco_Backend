package com.sanfrancisco.api.modules.ventas.dto.request;

import com.sanfrancisco.api.modules.ventas.enums.EstadoVenta;
import com.sanfrancisco.api.modules.ventas.enums.TipoVenta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Filtros opcionales para búsqueda paginada/dinámica de ventas.
 * Cualquier campo null se ignora en la specification resultante.
 */
public record VentaFilterRequest(
        String codigoVenta,
        EstadoVenta estado,
        TipoVenta tipoVenta,
        Integer usuarioId,
        Integer estanciaId,
        Integer huespedId,
        LocalDateTime fechaVentaDesde,
        LocalDateTime fechaVentaHasta,
        BigDecimal montoTotalMin,
        BigDecimal montoTotalMax
) {
}
