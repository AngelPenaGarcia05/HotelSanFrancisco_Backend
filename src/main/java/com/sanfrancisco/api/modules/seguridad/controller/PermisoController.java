package com.sanfrancisco.api.modules.seguridad.controller;

import com.sanfrancisco.api.modules.seguridad.dto.response.PermisoResponse;
import com.sanfrancisco.api.modules.seguridad.service.interfaces.PermisoService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permisos")
public class PermisoController {

    private final PermisoService service;

    public PermisoController(PermisoService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<PermisoResponse>> findAll() {
        return ApiResponse.ok(service.findAll());
    }
}
