package com.sanfrancisco.api.modules.rrhh.service.interfaces;

import com.sanfrancisco.api.modules.rrhh.dto.request.CreateBonoRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.UpdateBonoRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.BonoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BonoService {
    BonoResponse create(CreateBonoRequest request);
    BonoResponse update(Integer bonoId, UpdateBonoRequest request);
    BonoResponse findById(Integer bonoId);
    Page<BonoResponse> findAll(Pageable pageable);
    List<BonoResponse> findByUsuarioId(Integer usuarioId);
    List<BonoResponse> findByPagoNominaId(Integer pagoNominaId);
    void deleteById(Integer bonoId);
}
