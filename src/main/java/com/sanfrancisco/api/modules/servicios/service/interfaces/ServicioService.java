package com.sanfrancisco.api.modules.servicios.service.interfaces;

import com.sanfrancisco.api.modules.servicios.dto.request.CreateServicioRequest;
import com.sanfrancisco.api.modules.servicios.dto.request.ServicioFilterRequest;
import com.sanfrancisco.api.modules.servicios.dto.request.UpdateServicioRequest;
import com.sanfrancisco.api.modules.servicios.dto.response.ServicioResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ServicioService {

    ServicioResponse create(CreateServicioRequest request);

    ServicioResponse update(Integer servicioId, UpdateServicioRequest request);

    ServicioResponse findById(Integer servicioId);

    Page<ServicioResponse> search(ServicioFilterRequest filter, Pageable pageable);

    List<ServicioResponse> findByEstancia(Integer estanciaId);

    void deleteById(Integer servicioId);
}
