package com.sanfrancisco.api.modules.rrhh.controller;

import com.sanfrancisco.api.modules.rrhh.dto.request.CambiarEstadoPagoNominaRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.CreatePagoNominaRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.PagoNominaFilterRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.PagoNominaResponse;
import com.sanfrancisco.api.modules.rrhh.service.interfaces.PagoNominaService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import com.sanfrancisco.api.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pagos-nomina")
public class PagoNominaController {

    private final PagoNominaService pagoNominaService;

    public PagoNominaController(PagoNominaService pagoNominaService) {
        this.pagoNominaService = pagoNominaService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PagoNominaResponse>> create(@Valid @RequestBody CreatePagoNominaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(pagoNominaService.create(request), "Pago de nómina creado"));
    }

    @PatchMapping("/{id}/estado")
    public ApiResponse<PagoNominaResponse> cambiarEstado(@PathVariable Integer id,
                                                         @Valid @RequestBody CambiarEstadoPagoNominaRequest request) {
        return ApiResponse.ok(pagoNominaService.cambiarEstado(id, request), "Estado del pago actualizado");
    }

    @GetMapping("/{id}")
    public ApiResponse<PagoNominaResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(pagoNominaService.findById(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<PagoNominaResponse>> search(PagoNominaFilterRequest filter, Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(pagoNominaService.search(filter, pageable)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        pagoNominaService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.message("Pago de nómina eliminado"));
    }
}
