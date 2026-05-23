package com.sanfrancisco.api.modules.servicios.controller;

import com.sanfrancisco.api.modules.servicios.dto.request.CreateServicioRequest;
import com.sanfrancisco.api.modules.servicios.dto.request.ServicioFilterRequest;
import com.sanfrancisco.api.modules.servicios.dto.request.UpdateServicioRequest;
import com.sanfrancisco.api.modules.servicios.dto.response.ServicioResponse;
import com.sanfrancisco.api.modules.servicios.service.interfaces.ServicioService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import com.sanfrancisco.api.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/servicios")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ServicioResponse>> create(@Valid @RequestBody CreateServicioRequest request) {
        ServicioResponse created = servicioService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Servicio registrado exitosamente"));
    }

    @PutMapping("/{id}")
    public ApiResponse<ServicioResponse> update(@PathVariable Integer id,
                                                @Valid @RequestBody UpdateServicioRequest request) {
        return ApiResponse.ok(servicioService.update(id, request), "Servicio actualizado");
    }

    @GetMapping("/{id}")
    public ApiResponse<ServicioResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(servicioService.findById(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<ServicioResponse>> search(ServicioFilterRequest filter, Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(servicioService.search(filter, pageable)));
    }

    @GetMapping("/estancia/{estanciaId}")
    public ApiResponse<List<ServicioResponse>> findByEstancia(@PathVariable Integer estanciaId) {
        return ApiResponse.ok(servicioService.findByEstancia(estanciaId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        servicioService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.message("Servicio eliminado"));
    }
}
