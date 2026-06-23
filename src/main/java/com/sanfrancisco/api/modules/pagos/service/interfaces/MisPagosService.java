package com.sanfrancisco.api.modules.pagos.service.interfaces;

import com.sanfrancisco.api.modules.pagos.dto.response.MiPagoResponse;

import java.util.List;

public interface MisPagosService {

    /**
     * Pagos y saldos pendientes del cliente autenticado, ordenados por fecha desc.
     * Combina registros de pago reales (PAGADO) con saldos sintéticos por reserva (PENDIENTE).
     */
    List<MiPagoResponse> getMisPagos();

    /**
     * Pagos y saldo pendiente de UNA reserva del cliente autenticado.
     * Valida que la reserva pertenezca al usuario; si no existe o no le pertenece,
     * lanza {@link com.sanfrancisco.api.exception.ResourceNotFoundException}.
     */
    List<MiPagoResponse> getMisPagosPorReserva(Integer reservaId);
}
