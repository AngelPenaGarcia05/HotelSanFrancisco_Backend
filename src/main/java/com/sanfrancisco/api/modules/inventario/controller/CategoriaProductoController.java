package com.sanfrancisco.api.modules.inventario.controller;

import com.sanfrancisco.api.modules.inventario.dto.request.CreateCategoriaProductoRequest;
import com.sanfrancisco.api.modules.inventario.dto.request.UpdateCategoriaProductoRequest;
import com.sanfrancisco.api.modules.inventario.dto.response.CategoriaProductoResponse;
import com.sanfrancisco.api.modules.inventario.service.interfaces.CategoriaProductoService;
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
@RequestMapping("/api/v1/categorias-producto")
public class CategoriaProductoController {

    private final CategoriaProductoService service;

    public CategoriaProductoController(CategoriaProductoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoriaProductoResponse>> create(@Valid @RequestBody CreateCategoriaProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(service.create(request), "Categoría de producto creada"));
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoriaProductoResponse> update(@PathVariable Integer id,
                                                         @Valid @RequestBody UpdateCategoriaProductoRequest request) {
        return ApiResponse.ok(service.update(id, request), "Categoría de producto actualizada");
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoriaProductoResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(service.findById(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<CategoriaProductoResponse>> findAll(Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(service.findAll(pageable)));
    }

    @GetMapping("/estado/{estado}")
    public ApiResponse<List<CategoriaProductoResponse>> findByEstado(@PathVariable EstadoActivo estado) {
        return ApiResponse.ok(service.findByEstado(estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.ok(ApiResponse.message("Categoría de producto eliminada"));
    }
}
