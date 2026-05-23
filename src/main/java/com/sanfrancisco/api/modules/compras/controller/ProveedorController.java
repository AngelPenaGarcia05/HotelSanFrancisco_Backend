package com.sanfrancisco.api.modules.compras.controller;

import com.sanfrancisco.api.modules.compras.dto.request.CreateProveedorRequest;
import com.sanfrancisco.api.modules.compras.dto.request.ProveedorFilterRequest;
import com.sanfrancisco.api.modules.compras.dto.request.UpdateProveedorRequest;
import com.sanfrancisco.api.modules.compras.dto.response.ProveedorResponse;
import com.sanfrancisco.api.modules.compras.service.interfaces.ProveedorService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import com.sanfrancisco.api.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/proveedores")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProveedorResponse>> create(@Valid @RequestBody CreateProveedorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(proveedorService.create(request), "Proveedor creado exitosamente"));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProveedorResponse> update(@PathVariable Integer id,
                                                 @Valid @RequestBody UpdateProveedorRequest request) {
        return ApiResponse.ok(proveedorService.update(id, request), "Proveedor actualizado");
    }

    @GetMapping("/{id}")
    public ApiResponse<ProveedorResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(proveedorService.findById(id));
    }

    @GetMapping("/ruc/{rucNitCif}")
    public ApiResponse<ProveedorResponse> findByRuc(@PathVariable String rucNitCif) {
        return ApiResponse.ok(proveedorService.findByRuc(rucNitCif));
    }

    @GetMapping
    public ApiResponse<PageResponse<ProveedorResponse>> search(ProveedorFilterRequest filter, Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(proveedorService.search(filter, pageable)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        proveedorService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.message("Proveedor eliminado"));
    }
}
