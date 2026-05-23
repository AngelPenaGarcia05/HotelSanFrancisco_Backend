package com.sanfrancisco.api.modules.pagos.service.interfaces;

import com.sanfrancisco.api.modules.pagos.dto.request.CreatePagoRequest;
import com.sanfrancisco.api.modules.pagos.dto.request.PagoFilterRequest;
import com.sanfrancisco.api.modules.pagos.dto.request.UpdatePagoRequest;
import com.sanfrancisco.api.modules.pagos.dto.response.PagoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PagoService {

    PagoResponse create(CreatePagoRequest request);

    PagoResponse update(Integer pagoId, UpdatePagoRequest request);

    PagoResponse findById(Integer pagoId);

    Page<PagoResponse> search(PagoFilterRequest filter, Pageable pageable);

    List<PagoResponse> findByReserva(Integer reservaId);

    List<PagoResponse> findByVenta(Integer ventaId);

    void deleteById(Integer pagoId);
}
