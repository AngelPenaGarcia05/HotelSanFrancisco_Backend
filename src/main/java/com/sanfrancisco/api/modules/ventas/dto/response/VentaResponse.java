package com.sanfrancisco.api.modules.ventas.dto.response;

import com.sanfrancisco.api.modules.ventas.enums.EstadoVenta;
import com.sanfrancisco.api.modules.ventas.enums.TipoVenta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VentaResponse(
        Integer ventaId,
        String codigoVenta,
        TipoVenta tipoVenta,
        BigDecimal montoTotal,
        LocalDateTime fechaVenta,
        EstadoVenta estado,
        Integer usuarioId,
        String usuarioNombre,
        Integer estanciaId,
        Integer huespedId,
        String huespedNombre,
        List<DetalleVentaResponse> detalles,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {
}
