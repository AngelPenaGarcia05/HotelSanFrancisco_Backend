package com.sanfrancisco.api.modules.seguridad.service.interfaces;

import com.sanfrancisco.api.modules.seguridad.dto.response.PermisoResponse;

import java.util.List;

public interface PermisoService {

    List<PermisoResponse> findAll();
}
