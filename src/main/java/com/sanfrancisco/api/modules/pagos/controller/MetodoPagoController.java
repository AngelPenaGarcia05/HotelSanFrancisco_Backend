package com.sanfrancisco.api.modules.pagos.controller;

import com.sanfrancisco.api.modules.pagos.dto.request.CreateMetodoPagoRequest;
import com.sanfrancisco.api.modules.pagos.dto.request.UpdateMetodoPagoRequest;
import com.sanfrancisco.api.modules.pagos.dto.response.MetodoPagoResponse;
import com.sanfrancisco.api.modules.pagos.service.interfaces.MetodoPagoService;
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
@RequestMapping("/api/v1/metodos-pago")
public class MetodoPagoController {

    private final MetodoPagoService service;

    public MetodoPagoController(MetodoPagoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MetodoPagoResponse>> create(@Valid @RequestBody CreateMetodoPagoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(service.create(request), "Método de pago creado"));
    }

    @PutMapping("/{id}")
    public ApiResponse<MetodoPagoResponse> update(@PathVariable Integer id,
                                                  @Valid @RequestBody UpdateMetodoPagoRequest request) {
        return ApiResponse.ok(service.update(id, request), "Método de pago actualizado");
    }

    @GetMapping("/{id}")
    public ApiResponse<MetodoPagoResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(service.findById(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<MetodoPagoResponse>> findAll(Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(service.findAll(pageable)));
    }

    @GetMapping("/estado/{estado}")
    public ApiResponse<List<MetodoPagoResponse>> findByEstado(@PathVariable EstadoActivo estado) {
        return ApiResponse.ok(service.findByEstado(estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.ok(ApiResponse.message("Método de pago eliminado"));
    }
}
