package com.sanfrancisco.api.modules.pagos.service.interfaces;

import com.sanfrancisco.api.modules.pagos.dto.response.MiPagoResponse;

import java.util.List;

public interface MisPagosService {

    /**
     * Pagos y saldos pendientes del cliente autenticado, ordenados por fecha desc.
     * Combina registros de pago reales (PAGADO) con saldos sintéticos por reserva (PENDIENTE).
     */
    List<MiPagoResponse> getMisPagos();
}
