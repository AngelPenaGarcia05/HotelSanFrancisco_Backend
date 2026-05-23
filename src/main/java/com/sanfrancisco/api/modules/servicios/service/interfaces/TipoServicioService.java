package com.sanfrancisco.api.modules.servicios.service.interfaces;

import com.sanfrancisco.api.modules.servicios.dto.request.CreateTipoServicioRequest;
import com.sanfrancisco.api.modules.servicios.dto.request.UpdateTipoServicioRequest;
import com.sanfrancisco.api.modules.servicios.dto.response.TipoServicioResponse;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TipoServicioService {

    TipoServicioResponse create(CreateTipoServicioRequest request);

    TipoServicioResponse update(Integer tipoServicioId, UpdateTipoServicioRequest request);

    TipoServicioResponse findById(Integer tipoServicioId);

    Page<TipoServicioResponse> findAll(Pageable pageable);

    List<TipoServicioResponse> findByEstado(EstadoActivo estado);

    void deleteById(Integer tipoServicioId);
}
