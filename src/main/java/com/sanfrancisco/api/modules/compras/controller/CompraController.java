package com.sanfrancisco.api.modules.compras.controller;

import com.sanfrancisco.api.modules.compras.dto.request.CambiarEstadoCompraRequest;
import com.sanfrancisco.api.modules.compras.dto.request.CompraFilterRequest;
import com.sanfrancisco.api.modules.compras.dto.request.CreateCompraRequest;
import com.sanfrancisco.api.modules.compras.dto.request.UpdateCompraRequest;
import com.sanfrancisco.api.modules.compras.dto.response.CompraResponse;
import com.sanfrancisco.api.modules.compras.service.interfaces.CompraService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import com.sanfrancisco.api.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/compras")
public class CompraController {

    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CompraResponse>> create(@Valid @RequestBody CreateCompraRequest request) {
        CompraResponse created = compraService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Compra registrada exitosamente"));
    }

    @PutMapping("/{id}")
    public ApiResponse<CompraResponse> update(@PathVariable Integer id,
                                              @Valid @RequestBody UpdateCompraRequest request) {
        return ApiResponse.ok(compraService.update(id, request), "Compra actualizada");
    }

    @PatchMapping("/{id}/estado")
    public ApiResponse<CompraResponse> cambiarEstado(@PathVariable Integer id,
                                                     @Valid @RequestBody CambiarEstadoCompraRequest request) {
        return ApiResponse.ok(compraService.cambiarEstado(id, request), "Estado de compra actualizado");
    }

    @GetMapping("/{id}")
    public ApiResponse<CompraResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(compraService.findById(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<CompraResponse>> search(CompraFilterRequest filter, Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(compraService.search(filter, pageable)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        compraService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.message("Compra eliminada"));
    }
}
