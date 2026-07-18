package com.sanfrancisco.api.modules.pagos.service.interfaces;

import com.sanfrancisco.api.modules.pagos.dto.request.CreatePagoRequest;
import com.sanfrancisco.api.modules.pagos.dto.request.PagoFilterRequest;
import com.sanfrancisco.api.modules.pagos.dto.request.UpdatePagoRequest;
import com.sanfrancisco.api.modules.pagos.dto.response.PagoResponse;
import com.sanfrancisco.api.modules.pagos.dto.response.ResumenPagosReservaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PagoService {

    PagoResponse create(CreatePagoRequest request);

    /**
     * Cobro inicial en efectivo de una reserva PENDIENTE (recepción/admin):
     * registra el pago por el adelanto derivado de la modalidad y confirma la
     * reserva. Es el equivalente presencial de la autorización Niubiz.
     */
    PagoResponse registrarPagoInicialEfectivo(Integer reservaId);

    PagoResponse update(Integer pagoId, UpdatePagoRequest request);

    PagoResponse findById(Integer pagoId);

    Page<PagoResponse> search(PagoFilterRequest filter, Pageable pageable);

    List<PagoResponse> findByReserva(Integer reservaId);

    ResumenPagosReservaResponse resumenByReserva(Integer reservaId);

    List<PagoResponse> findByVenta(Integer ventaId);

    void deleteById(Integer pagoId);
}
