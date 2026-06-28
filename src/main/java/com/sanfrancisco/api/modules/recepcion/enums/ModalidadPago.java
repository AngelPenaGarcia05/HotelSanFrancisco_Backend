package com.sanfrancisco.api.modules.recepcion.enums;

/**
 * Modalidad de pago inicial de una reserva (política del hotel).
 * PARCIAL = adelanto del 50 % del total; TOTAL = pago completo (100 %).
 * El monto del adelanto lo deriva el backend a partir del total; nunca se acepta
 * un monto libre desde el cliente.
 */
public enum ModalidadPago {
    PARCIAL,
    TOTAL
}
