package com.sanfrancisco.api.modules.dashboard.service;

import com.sanfrancisco.api.modules.dashboard.dto.response.DashboardResponse;

public interface DashboardService {

    /**
     * Construye el dashboard del usuario autenticado, incluyendo únicamente
     * las tarjetas para las que posee el permiso granular correspondiente.
     */
    DashboardResponse build();
}
