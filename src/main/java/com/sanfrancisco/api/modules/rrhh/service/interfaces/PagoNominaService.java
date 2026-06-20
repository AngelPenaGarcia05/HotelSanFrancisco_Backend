package com.sanfrancisco.api.modules.rrhh.service.interfaces;

import com.sanfrancisco.api.modules.rrhh.dto.request.CambiarEstadoPagoNominaRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.CreatePagoNominaRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.PagoNominaFilterRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.PagoNominaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PagoNominaService {
    PagoNominaResponse create(CreatePagoNominaRequest request);
    PagoNominaResponse cambiarEstado(Integer pagoNominaId, CambiarEstadoPagoNominaRequest request);
    PagoNominaResponse findById(Integer pagoNominaId);
    Page<PagoNominaResponse> search(PagoNominaFilterRequest filter, Pageable pageable);
    void deleteById(Integer pagoNominaId);
}
