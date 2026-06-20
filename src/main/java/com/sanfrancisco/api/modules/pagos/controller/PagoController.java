package com.sanfrancisco.api.modules.pagos.controller;

import com.sanfrancisco.api.modules.pagos.dto.request.CreatePagoRequest;
import com.sanfrancisco.api.modules.pagos.dto.request.PagoFilterRequest;
import com.sanfrancisco.api.modules.pagos.dto.request.UpdatePagoRequest;
import com.sanfrancisco.api.modules.pagos.dto.response.PagoResponse;
import com.sanfrancisco.api.modules.pagos.service.interfaces.PagoService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import com.sanfrancisco.api.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PagoResponse>> create(@Valid @RequestBody CreatePagoRequest request) {
        PagoResponse created = pagoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Pago registrado exitosamente"));
    }

    @PutMapping("/{id}")
    public ApiResponse<PagoResponse> update(@PathVariable Integer id,
                                            @Valid @RequestBody UpdatePagoRequest request) {
        return ApiResponse.ok(pagoService.update(id, request), "Pago actualizado");
    }

    @GetMapping("/{id}")
    public ApiResponse<PagoResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(pagoService.findById(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<PagoResponse>> search(PagoFilterRequest filter, Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(pagoService.search(filter, pageable)));
    }

    @GetMapping("/reserva/{reservaId}")
    public ApiResponse<List<PagoResponse>> findByReserva(@PathVariable Integer reservaId) {
        return ApiResponse.ok(pagoService.findByReserva(reservaId));
    }

    @GetMapping("/venta/{ventaId}")
    public ApiResponse<List<PagoResponse>> findByVenta(@PathVariable Integer ventaId) {
        return ApiResponse.ok(pagoService.findByVenta(ventaId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        pagoService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.message("Pago eliminado"));
    }
}
