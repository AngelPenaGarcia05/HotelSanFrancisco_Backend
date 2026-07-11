package com.sanfrancisco.api.modules.rrhh.controller;

import com.sanfrancisco.api.modules.rrhh.dto.request.AsignarHorarioRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.DetalleHorarioResponse;
import com.sanfrancisco.api.modules.rrhh.service.interfaces.DetalleHorarioService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asignaciones-horario")
public class AsignacionHorarioController {

    private final DetalleHorarioService detalleHorarioService;

    public AsignacionHorarioController(DetalleHorarioService detalleHorarioService) {
        this.detalleHorarioService = detalleHorarioService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DetalleHorarioResponse>> asignar(@Valid @RequestBody AsignarHorarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(detalleHorarioService.asignar(request), "Horario asignado exitosamente al usuario"));
    }

    @PutMapping("/{detalleHorarioId}")
    public ApiResponse<DetalleHorarioResponse> update(@PathVariable Integer detalleHorarioId,
                                                      @Valid @RequestBody AsignarHorarioRequest request) {
        return ApiResponse.ok(detalleHorarioService.update(detalleHorarioId, request), "Asignación actualizada");
    }

    @DeleteMapping("/{detalleHorarioId}")
    public ResponseEntity<ApiResponse<Void>> remover(@PathVariable Integer detalleHorarioId) {
        detalleHorarioService.remover(detalleHorarioId);
        return ResponseEntity.ok(ApiResponse.message("Horario removido del usuario"));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ApiResponse<List<DetalleHorarioResponse>> findByUsuarioId(@PathVariable Integer usuarioId) {
        return ApiResponse.ok(detalleHorarioService.findByUsuarioId(usuarioId));
    }
}
