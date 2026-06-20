package com.sanfrancisco.api.modules.solicitudes.service.interfaces;

import com.sanfrancisco.api.modules.solicitudes.dto.request.AsignarResponsableRequest;
import com.sanfrancisco.api.modules.solicitudes.dto.request.CambiarEstadoSolicitudRequest;
import com.sanfrancisco.api.modules.solicitudes.dto.request.CreateSolicitudRequest;
import com.sanfrancisco.api.modules.solicitudes.dto.request.RegistrarSeguimientoRequest;
import com.sanfrancisco.api.modules.solicitudes.dto.request.SolicitudFilterRequest;
import com.sanfrancisco.api.modules.solicitudes.dto.request.UpdateSolicitudRequest;
import com.sanfrancisco.api.modules.solicitudes.dto.response.SeguimientoSolicitudResponse;
import com.sanfrancisco.api.modules.solicitudes.dto.response.SolicitudReporteResponse;
import com.sanfrancisco.api.modules.solicitudes.dto.response.SolicitudResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SolicitudService {

    SolicitudResponse create(CreateSolicitudRequest request);

    SolicitudResponse update(Integer solicitudId, UpdateSolicitudRequest request);

    SolicitudResponse findById(Integer solicitudId);

    Page<SolicitudResponse> search(SolicitudFilterRequest filter, Pageable pageable);

    List<SeguimientoSolicitudResponse> getSeguimientos(Integer solicitudId);

    SolicitudResponse asignarResponsable(Integer solicitudId, AsignarResponsableRequest request);

    SolicitudResponse cambiarEstado(Integer solicitudId, CambiarEstadoSolicitudRequest request);

    SeguimientoSolicitudResponse registrarObservacion(Integer solicitudId, RegistrarSeguimientoRequest request);

    SolicitudReporteResponse generarReporte();

    void deleteById(Integer solicitudId);
}
