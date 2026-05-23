package com.sanfrancisco.api.modules.rrhh.controller;

import com.sanfrancisco.api.modules.rrhh.dto.request.AsistenciaFilterRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.CreateAsistenciaRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.UpdateAsistenciaRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.AsistenciaResponse;
import com.sanfrancisco.api.modules.rrhh.service.interfaces.AsistenciaService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import com.sanfrancisco.api.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/asistencias")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    public AsistenciaController(AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AsistenciaResponse>> create(@Valid @RequestBody CreateAsistenciaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(asistenciaService.create(request), "Registro de asistencia creado"));
    }

    @PutMapping("/{id}")
    public ApiResponse<AsistenciaResponse> update(@PathVariable Integer id,
                                                  @Valid @RequestBody UpdateAsistenciaRequest request) {
        return ApiResponse.ok(asistenciaService.update(id, request), "Registro de asistencia actualizado");
    }

    @GetMapping("/{id}")
    public ApiResponse<AsistenciaResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(asistenciaService.findById(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<AsistenciaResponse>> search(AsistenciaFilterRequest filter, Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(asistenciaService.search(filter, pageable)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        asistenciaService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.message("Registro de asistencia eliminado"));
    }
}
