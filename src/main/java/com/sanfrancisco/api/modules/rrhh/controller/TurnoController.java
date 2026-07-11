package com.sanfrancisco.api.modules.rrhh.controller;

import com.sanfrancisco.api.modules.rrhh.dto.request.ActualizarTurnoRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.GenerarTurnosRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.GenerarTurnosResponse;
import com.sanfrancisco.api.modules.rrhh.dto.response.TurnoResponse;
import com.sanfrancisco.api.modules.rrhh.service.interfaces.TurnoService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/turnos")
public class TurnoController {

    private final TurnoService turnoService;

    public TurnoController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    @PostMapping("/generar")
    public ResponseEntity<ApiResponse<GenerarTurnosResponse>> generar(
            @Valid @RequestBody GenerarTurnosRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(turnoService.generar(request), "Turnos generados desde la plantilla"));
    }

    @GetMapping
    public ApiResponse<List<TurnoResponse>> listarPorRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ApiResponse.ok(turnoService.listarPorRango(desde, hasta));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ApiResponse<List<TurnoResponse>> listarPorUsuario(
            @PathVariable Integer usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ApiResponse.ok(turnoService.listarPorUsuario(usuarioId, desde, hasta));
    }

    @PatchMapping("/{turnoId}")
    public ApiResponse<TurnoResponse> actualizar(
            @PathVariable Integer turnoId,
            @Valid @RequestBody ActualizarTurnoRequest request) {
        return ApiResponse.ok(turnoService.actualizar(turnoId, request), "Turno actualizado");
    }

    @DeleteMapping("/{turnoId}")
    public ResponseEntity<ApiResponse<Void>> cancelar(@PathVariable Integer turnoId) {
        turnoService.cancelar(turnoId);
        return ResponseEntity.ok(ApiResponse.message("Turno cancelado"));
    }
}
