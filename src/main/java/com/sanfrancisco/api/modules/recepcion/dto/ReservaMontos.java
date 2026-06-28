package com.sanfrancisco.api.modules.recepcion.dto;

import com.sanfrancisco.api.modules.recepcion.enums.ModalidadPago;

import java.math.BigDecimal;

/**
 * Montos de una reserva calculados server-side (fuente de verdad).
 * El front nunca los provee: el backend recalcula impuesto, total y adelanto
 * a partir del subtotal real, el descuento validado y la modalidad de pago.
 */
public record ReservaMontos(
        String codReserva,
        BigDecimal subtotal,
        BigDecimal descuento,
        BigDecimal impuesto,
        BigDecimal montoTotal,
        BigDecimal adelanto,
        ModalidadPago modalidadPago
) {
}
