package com.sanfrancisco.api.modules.inventario.controller;

import com.sanfrancisco.api.modules.inventario.dto.request.AjustarStockRequest;
import com.sanfrancisco.api.modules.inventario.dto.request.CreateProductoRequest;
import com.sanfrancisco.api.modules.inventario.dto.request.ProductoFilterRequest;
import com.sanfrancisco.api.modules.inventario.dto.request.UpdateProductoRequest;
import com.sanfrancisco.api.modules.inventario.dto.response.ProductoResponse;
import com.sanfrancisco.api.modules.inventario.service.interfaces.ProductoService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import com.sanfrancisco.api.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductoResponse>> create(@Valid @RequestBody CreateProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(productoService.create(request), "Producto creado exitosamente"));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductoResponse> update(@PathVariable Integer id,
                                                @Valid @RequestBody UpdateProductoRequest request) {
        return ApiResponse.ok(productoService.update(id, request), "Producto actualizado");
    }

    @PatchMapping("/{id}/stock")
    public ApiResponse<ProductoResponse> ajustarStock(@PathVariable Integer id,
                                                      @Valid @RequestBody AjustarStockRequest request) {
        return ApiResponse.ok(productoService.ajustarStock(id, request), "Stock ajustado");
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductoResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(productoService.findById(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<ProductoResponse>> search(ProductoFilterRequest filter, Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(productoService.search(filter, pageable)));
    }

    @GetMapping("/bajo-stock")
    public ApiResponse<List<ProductoResponse>> findBajoStock() {
        return ApiResponse.ok(productoService.findBajoStock());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        productoService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.message("Producto eliminado"));
    }
}
