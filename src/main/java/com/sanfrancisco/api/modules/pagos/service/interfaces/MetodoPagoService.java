package com.sanfrancisco.api.modules.pagos.service.interfaces;

import com.sanfrancisco.api.modules.pagos.dto.request.CreateMetodoPagoRequest;
import com.sanfrancisco.api.modules.pagos.dto.request.UpdateMetodoPagoRequest;
import com.sanfrancisco.api.modules.pagos.dto.response.MetodoPagoResponse;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MetodoPagoService {

    MetodoPagoResponse create(CreateMetodoPagoRequest request);

    MetodoPagoResponse update(Integer metodoPagoId, UpdateMetodoPagoRequest request);

    MetodoPagoResponse findById(Integer metodoPagoId);

    Page<MetodoPagoResponse> findAll(Pageable pageable);

    List<MetodoPagoResponse> findByEstado(EstadoActivo estado);

    void deleteById(Integer metodoPagoId);
}
