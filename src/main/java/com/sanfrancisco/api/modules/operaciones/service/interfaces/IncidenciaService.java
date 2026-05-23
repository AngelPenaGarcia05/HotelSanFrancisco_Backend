package com.sanfrancisco.api.modules.operaciones.service.interfaces;

import com.sanfrancisco.api.modules.operaciones.dto.request.CambiarEstadoIncidenciaRequest;
import com.sanfrancisco.api.modules.operaciones.dto.request.CreateIncidenciaRequest;
import com.sanfrancisco.api.modules.operaciones.dto.request.IncidenciaFilterRequest;
import com.sanfrancisco.api.modules.operaciones.dto.request.UpdateIncidenciaRequest;
import com.sanfrancisco.api.modules.operaciones.dto.response.IncidenciaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IncidenciaService {

    IncidenciaResponse create(CreateIncidenciaRequest request);

    IncidenciaResponse update(Integer incidenciaId, UpdateIncidenciaRequest request);

    IncidenciaResponse findById(Integer incidenciaId);

    Page<IncidenciaResponse> search(IncidenciaFilterRequest filter, Pageable pageable);

    IncidenciaResponse cambiarEstado(Integer incidenciaId, CambiarEstadoIncidenciaRequest request);

    void deleteById(Integer incidenciaId);
}
