package com.sanfrancisco.api.modules.recepcion.controller;

import com.sanfrancisco.api.modules.recepcion.dto.request.CheckInRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.CheckOutRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.CreateHabitacionRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.UpdateHabitacionRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.CheckOutLiquidacionResponse;
import com.sanfrancisco.api.modules.recepcion.dto.response.HabitacionResponse;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoHabitacion;
import com.sanfrancisco.api.modules.recepcion.service.interfaces.HabitacionService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/habitaciones")
public class HabitacionController {

    private final HabitacionService service;

    public HabitacionController(HabitacionService service) {
        this.service = service;
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<HabitacionResponse>> create(
            @Valid @RequestBody CreateHabitacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(service.create(request), "Habitación creada"));
    }

    @PutMapping("/{id}")
    public ApiResponse<HabitacionResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateHabitacionRequest request) {
        return ApiResponse.ok(service.update(id, request), "Habitación actualizada");
    }

    @GetMapping("/{id}")
    public ApiResponse<HabitacionResponse> findById(@PathVariable Integer id) {
        return ApiResponse.ok(service.findById(id));
    }

    @GetMapping
    public ApiResponse<List<HabitacionResponse>> findAll(
            @RequestParam(required = false) EstadoHabitacion estado,
            @RequestParam(required = false) Integer piso) {
        if (estado != null && piso != null) {
            return ApiResponse.ok(service.findByEstado(estado).stream()
                    .filter(h -> h.piso().equals(piso)).toList());
        }
        if (estado != null) return ApiResponse.ok(service.findByEstado(estado));
        if (piso != null)   return ApiResponse.ok(service.findByPiso(piso));
        return ApiResponse.ok(service.findAll());
    }

    @PatchMapping("/{id}/estado")
    public ApiResponse<HabitacionResponse> cambiarEstado(
            @PathVariable Integer id,
            @RequestParam EstadoHabitacion nuevoEstado) {
        return ApiResponse.ok(service.cambiarEstado(id, nuevoEstado), "Estado actualizado");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.ok(ApiResponse.message("Habitación eliminada"));
    }

    // ── OPERACIONES ───────────────────────────────────────────────────────────

    /** Proceso de Check-in: valida reserva CONFIRMADA y pone habitaciones en OCUPADA. */
    @PostMapping("/checkin")
    public ApiResponse<HabitacionResponse> checkIn(@Valid @RequestBody CheckInRequest request) {
        return ApiResponse.ok(service.checkIn(request), "Check-in realizado con éxito");
    }

    /** Proceso de Check-out: libera habitaciones, cambia a LIMPIEZA, retorna liquidación. */
    @PostMapping("/checkout")
    public ApiResponse<CheckOutLiquidacionResponse> checkOut(@Valid @RequestBody CheckOutRequest request) {
        return ApiResponse.ok(service.checkOut(request), "Check-out realizado. Habitaciones en cola de limpieza.");
    }

    /** Lista habitaciones pendientes de limpieza. */
    @GetMapping("/limpieza")
    public ApiResponse<List<HabitacionResponse>> pendientesLimpieza() {
        return ApiResponse.ok(service.findLimpieza());
    }

    /** Confirma que la limpieza fue completada — habitación vuelve a DISPONIBLE. */
    @PatchMapping("/{id}/limpieza-completada")
    public ApiResponse<HabitacionResponse> limpiezaCompletada(@PathVariable Integer id) {
        return ApiResponse.ok(service.registrarLimpiezaCompletada(id), "Habitación disponible");
    }
}
