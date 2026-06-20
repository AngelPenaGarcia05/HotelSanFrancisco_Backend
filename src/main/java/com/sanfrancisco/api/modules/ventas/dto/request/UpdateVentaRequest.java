package com.sanfrancisco.api.modules.ventas.dto.request;

import com.sanfrancisco.api.modules.ventas.enums.TipoVenta;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDateTime;

/**
 * Update parcial: todos los campos son opcionales; solo se aplican los presentes (no-null).
 * Los detalles no se modifican vía update; debe usarse una nueva venta o anular y recrear.
 * Las transiciones de estado se manejan vía endpoint dedicado.
 */
public record UpdateVentaRequest(

        TipoVenta tipoVenta,

        @PastOrPresent(message = "La fecha de venta no puede ser futura")
        LocalDateTime fechaVenta,

        Integer estanciaId,

        Integer huespedId
) {
}
