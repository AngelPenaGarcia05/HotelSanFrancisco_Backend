package com.sanfrancisco.api.modules.recepcion.service.interfaces;

import com.sanfrancisco.api.modules.recepcion.dto.request.CambiarEstadoReservaRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.CreateReservaRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.ReservaFilterRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.UpdateReservaRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.ReservaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservaService {

    ReservaResponse create(CreateReservaRequest request);

    ReservaResponse update(Integer reservaId, UpdateReservaRequest request);

    ReservaResponse findById(Integer reservaId);

    ReservaResponse findByCodigo(String codReserva);

    Page<ReservaResponse> search(ReservaFilterRequest filter, Pageable pageable);

    ReservaResponse cambiarEstado(Integer reservaId, CambiarEstadoReservaRequest request);

    void deleteById(Integer reservaId);
}
