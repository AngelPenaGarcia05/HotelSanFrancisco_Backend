package com.sanfrancisco.api.modules.recepcion.controller;

import com.sanfrancisco.api.modules.recepcion.dto.request.CambiarEstadoReservaRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.CreateReservaRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.ReservaFilterRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.UpdateReservaRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.ReservaResponse;
import com.sanfrancisco.api.modules.recepcion.service.interfaces.ReservaService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import com.sanfrancisco.api.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReservaResponse>> create(@Valid @RequestBody CreateReservaRequest request) {
        ReservaResponse created = reservaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Reserva creada exitosamente"));
    }

    @PutMapping("/{id}")
    public ApiResponse<ReservaResponse> update(@PathVariable Integer id,
                                               @Valid @RequestBody UpdateReservaRequest request) {
        return ApiResponse.ok(reservaService.update(id, request), "Reserva actualizada");
    }

    @PatchMapping("/{id}/estado")
    public ApiResponse<ReservaResponse> cambiarEstado(@PathVariable Integer id,
                                                      @Valid @RequestBody CambiarEstadoReservaRequest request) {
        return ApiResponse.ok(reservaService.cambiarEstado(id, request), "Estado de reserva actualizado");
    }

    @GetMapping("/{id}")
    public ApiResponse<ReservaResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(reservaService.findById(id));
    }

    @GetMapping("/codigo/{codReserva}")
    public ApiResponse<ReservaResponse> findByCodigo(@PathVariable String codReserva) {
        return ApiResponse.ok(reservaService.findByCodigo(codReserva));
    }

    @GetMapping
    public ApiResponse<PageResponse<ReservaResponse>> search(ReservaFilterRequest filter, Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(reservaService.search(filter, pageable)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        reservaService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.message("Reserva eliminada"));
    }
}
