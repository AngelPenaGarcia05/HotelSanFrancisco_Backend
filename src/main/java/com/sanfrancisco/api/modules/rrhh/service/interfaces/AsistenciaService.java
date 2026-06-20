package com.sanfrancisco.api.modules.rrhh.service.interfaces;

import com.sanfrancisco.api.modules.rrhh.dto.request.AsistenciaFilterRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.CreateAsistenciaRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.UpdateAsistenciaRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.AsistenciaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AsistenciaService {
    AsistenciaResponse create(CreateAsistenciaRequest request);
    AsistenciaResponse update(Integer asistenciaId, UpdateAsistenciaRequest request);
    AsistenciaResponse findById(Integer asistenciaId);
    Page<AsistenciaResponse> search(AsistenciaFilterRequest filter, Pageable pageable);
    void deleteById(Integer asistenciaId);
}
