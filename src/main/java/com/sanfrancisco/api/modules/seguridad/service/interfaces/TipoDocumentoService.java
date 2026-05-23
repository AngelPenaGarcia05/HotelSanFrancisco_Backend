package com.sanfrancisco.api.modules.seguridad.service.interfaces;

import com.sanfrancisco.api.modules.seguridad.dto.request.CreateTipoDocumentoRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.UpdateTipoDocumentoRequest;
import com.sanfrancisco.api.modules.seguridad.dto.response.TipoDocumentoResponse;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TipoDocumentoService {

    TipoDocumentoResponse create(CreateTipoDocumentoRequest request);

    TipoDocumentoResponse update(Integer id, UpdateTipoDocumentoRequest request);

    TipoDocumentoResponse findById(Integer id);

    Page<TipoDocumentoResponse> findAll(Pageable pageable);

    List<TipoDocumentoResponse> findByEstado(EstadoActivo estado);

    void deleteById(Integer id);
}
