package com.sanfrancisco.api.modules.ventas.controller;

import com.sanfrancisco.api.modules.ventas.dto.request.CambiarEstadoVentaRequest;
import com.sanfrancisco.api.modules.ventas.dto.request.CreateVentaRequest;
import com.sanfrancisco.api.modules.ventas.dto.request.UpdateVentaRequest;
import com.sanfrancisco.api.modules.ventas.dto.request.VentaFilterRequest;
import com.sanfrancisco.api.modules.ventas.dto.response.VentaResponse;
import com.sanfrancisco.api.modules.ventas.service.interfaces.VentaService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import com.sanfrancisco.api.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VentaResponse>> create(@Valid @RequestBody CreateVentaRequest request) {
        VentaResponse created = ventaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Venta registrada exitosamente"));
    }

    @PutMapping("/{id}")
    public ApiResponse<VentaResponse> update(@PathVariable Integer id,
                                             @Valid @RequestBody UpdateVentaRequest request) {
        return ApiResponse.ok(ventaService.update(id, request), "Venta actualizada");
    }

    @PatchMapping("/{id}/estado")
    public ApiResponse<VentaResponse> cambiarEstado(@PathVariable Integer id,
                                                    @Valid @RequestBody CambiarEstadoVentaRequest request) {
        return ApiResponse.ok(ventaService.cambiarEstado(id, request), "Estado de venta actualizado");
    }

    @GetMapping("/{id}")
    public ApiResponse<VentaResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(ventaService.findById(id));
    }

    @GetMapping("/codigo/{codigoVenta}")
    public ApiResponse<VentaResponse> findByCodigo(@PathVariable String codigoVenta) {
        return ApiResponse.ok(ventaService.findByCodigo(codigoVenta));
    }

    @GetMapping
    public ApiResponse<PageResponse<VentaResponse>> search(VentaFilterRequest filter, Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(ventaService.search(filter, pageable)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        ventaService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.message("Venta eliminada"));
    }
}
