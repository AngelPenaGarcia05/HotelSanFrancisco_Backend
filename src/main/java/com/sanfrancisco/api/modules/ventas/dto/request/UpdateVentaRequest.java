package com.sanfrancisco.api.modules.ventas.dto.request;

import com.sanfrancisco.api.modules.ventas.enums.TipoVenta;

/**
 * Update parcial: todos los campos son opcionales; solo se aplican los presentes (no-null).
 * Los detalles no se modifican vía update; debe usarse una nueva venta o anular y recrear.
 * Las transiciones de estado se manejan vía endpoint dedicado.
 * La fecha de venta es server-side y no puede modificarse desde el cliente.
 */
public record UpdateVentaRequest(

        TipoVenta tipoVenta,

        Integer estanciaId,

        Integer huespedId
) {
}
