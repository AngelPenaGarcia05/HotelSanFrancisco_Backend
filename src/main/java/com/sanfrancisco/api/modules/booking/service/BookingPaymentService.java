package com.sanfrancisco.api.modules.booking.service;

import com.sanfrancisco.api.modules.booking.dto.BookingConfirmationResponse;
import com.sanfrancisco.api.modules.booking.dto.ConfirmarPagoRequest;
import com.sanfrancisco.api.modules.booking.dto.CrearSesionPagoResponse;

public interface BookingPaymentService {

    /** Crea la sesión de checkout Niubiz para una pre-reserva PENDIENTE. */
    CrearSesionPagoResponse crearSesionPago(Integer reservaId, String clientIp);

    /**
     * Autoriza el cobro con el transaction token del checkout y, si Niubiz
     * aprueba, confirma la reserva y registra el pago.
     */
    BookingConfirmationResponse confirmarPago(ConfirmarPagoRequest request);

    /**
     * Confirmación de una transacción ya AUTORIZADA (la usa el frontend tras
     * el redirect de retorno del checkout, cuando perdió el estado en memoria).
     */
    BookingConfirmationResponse confirmacionPorPurchaseNumber(String purchaseNumber);
}
