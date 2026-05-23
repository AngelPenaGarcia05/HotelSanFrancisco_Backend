package com.sanfrancisco.api.modules.operaciones.controller;

import com.sanfrancisco.api.modules.operaciones.dto.request.CambiarEstadoIncidenciaRequest;
import com.sanfrancisco.api.modules.operaciones.dto.request.CreateIncidenciaRequest;
import com.sanfrancisco.api.modules.operaciones.dto.request.IncidenciaFilterRequest;
import com.sanfrancisco.api.modules.operaciones.dto.request.UpdateIncidenciaRequest;
import com.sanfrancisco.api.modules.operaciones.dto.response.IncidenciaResponse;
import com.sanfrancisco.api.modules.operaciones.service.interfaces.IncidenciaService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import com.sanfrancisco.api.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/incidencias")
public class IncidenciaController {

    private final IncidenciaService incidenciaService;

    public IncidenciaController(IncidenciaService incidenciaService) {
        this.incidenciaService = incidenciaService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<IncidenciaResponse>> create(@Valid @RequestBody CreateIncidenciaRequest request) {
        IncidenciaResponse created = incidenciaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Incidencia registrada exitosamente"));
    }

    @PutMapping("/{id}")
    public ApiResponse<IncidenciaResponse> update(@PathVariable Integer id,
                                                  @Valid @RequestBody UpdateIncidenciaRequest request) {
        return ApiResponse.ok(incidenciaService.update(id, request), "Incidencia actualizada");
    }

    @PatchMapping("/{id}/estado")
    public ApiResponse<IncidenciaResponse> cambiarEstado(@PathVariable Integer id,
                                                         @Valid @RequestBody CambiarEstadoIncidenciaRequest request) {
        return ApiResponse.ok(incidenciaService.cambiarEstado(id, request), "Estado de incidencia actualizado");
    }

    @GetMapping("/{id}")
    public ApiResponse<IncidenciaResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(incidenciaService.findById(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<IncidenciaResponse>> search(IncidenciaFilterRequest filter, Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(incidenciaService.search(filter, pageable)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        incidenciaService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.message("Incidencia eliminada"));
    }
}
