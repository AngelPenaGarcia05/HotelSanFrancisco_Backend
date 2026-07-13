package com.sanfrancisco.api.modules.ventas.dto.request;

import com.sanfrancisco.api.modules.ventas.enums.TipoVenta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

/**
 * El código de venta y la fecha de venta NO se reciben del cliente:
 * ambos se generan/fijan server-side en el servicio (código único VEN-AAAA-NNNNNN
 * y fecha/hora actual del servidor).
 */
public record CreateVentaRequest(

        @NotNull(message = "El tipo de venta es obligatorio")
        TipoVenta tipoVenta,

        @NotNull(message = "El usuario es obligatorio")
        Integer usuarioId,

        Integer estanciaId,

        Integer huespedId,

        @NotEmpty(message = "La venta debe tener al menos un detalle")
        @Valid
        List<CreateDetalleVentaRequest> detalles
) {
}
