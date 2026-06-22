package com.sanfrancisco.api.modules.pagos.dto.response;

import java.math.BigDecimal;

/**
 * Representa una fila de la página "Mis pagos" del cliente.
 * Los nombres de los campos coinciden EXACTAMENTE con la interfaz
 * {@code PagoClienteItem} del frontend; no cambiar sin coordinar.
 *
 * <p>Una fila puede ser:</p>
 * <ul>
 *   <li>PAGADO: un registro de pago real ({@code pagoId} no nulo).</li>
 *   <li>PENDIENTE: saldo sintético de una reserva con deuda ({@code pagoId} nulo).</li>
 * </ul>
 */
public record MiPagoResponse(
        Integer pagoId,
        Integer reservaId,
        String codReserva,
        String habitacion,
        String estado,
        String fecha,
        String metodoPago,
        BigDecimal monto,
        String facturaUrl
) {}
