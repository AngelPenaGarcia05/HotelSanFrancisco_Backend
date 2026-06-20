package com.sanfrancisco.api.modules.servicios.controller;

import com.sanfrancisco.api.modules.servicios.dto.request.CreateTipoServicioRequest;
import com.sanfrancisco.api.modules.servicios.dto.request.UpdateTipoServicioRequest;
import com.sanfrancisco.api.modules.servicios.dto.response.TipoServicioResponse;
import com.sanfrancisco.api.modules.servicios.service.interfaces.TipoServicioService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import com.sanfrancisco.api.shared.api.PageResponse;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tipos-servicio")
public class TipoServicioController {

    private final TipoServicioService service;

    public TipoServicioController(TipoServicioService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TipoServicioResponse>> create(@Valid @RequestBody CreateTipoServicioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(service.create(request), "Tipo de servicio creado"));
    }

    @PutMapping("/{id}")
    public ApiResponse<TipoServicioResponse> update(@PathVariable Integer id,
                                                    @Valid @RequestBody UpdateTipoServicioRequest request) {
        return ApiResponse.ok(service.update(id, request), "Tipo de servicio actualizado");
    }

    @GetMapping("/{id}")
    public ApiResponse<TipoServicioResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(service.findById(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<TipoServicioResponse>> findAll(Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(service.findAll(pageable)));
    }

    @GetMapping("/estado/{estado}")
    public ApiResponse<List<TipoServicioResponse>> findByEstado(@PathVariable EstadoActivo estado) {
        return ApiResponse.ok(service.findByEstado(estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.ok(ApiResponse.message("Tipo de servicio eliminado"));
    }
}
