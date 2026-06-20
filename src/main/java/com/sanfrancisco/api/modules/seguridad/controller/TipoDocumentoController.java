package com.sanfrancisco.api.modules.seguridad.controller;

import com.sanfrancisco.api.modules.seguridad.dto.request.CreateTipoDocumentoRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.UpdateTipoDocumentoRequest;
import com.sanfrancisco.api.modules.seguridad.dto.response.TipoDocumentoResponse;
import com.sanfrancisco.api.modules.seguridad.service.interfaces.TipoDocumentoService;
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
@RequestMapping("/api/v1/tipos-documento")
public class TipoDocumentoController {

    private final TipoDocumentoService service;

    public TipoDocumentoController(TipoDocumentoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TipoDocumentoResponse>> create(@Valid @RequestBody CreateTipoDocumentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(service.create(request), "Tipo de documento creado"));
    }

    @PutMapping("/{id}")
    public ApiResponse<TipoDocumentoResponse> update(@PathVariable Integer id,
                                                     @Valid @RequestBody UpdateTipoDocumentoRequest request) {
        return ApiResponse.ok(service.update(id, request), "Tipo de documento actualizado");
    }

    @GetMapping("/{id}")
    public ApiResponse<TipoDocumentoResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(service.findById(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<TipoDocumentoResponse>> findAll(Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(service.findAll(pageable)));
    }

    @GetMapping("/estado/{estado}")
    public ApiResponse<List<TipoDocumentoResponse>> findByEstado(@PathVariable EstadoActivo estado) {
        return ApiResponse.ok(service.findByEstado(estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.ok(ApiResponse.message("Tipo de documento eliminado"));
    }
}
