package com.sanfrancisco.api.modules.rrhh.controller;

import com.sanfrancisco.api.modules.rrhh.dto.response.AsistenciaResponse;
import com.sanfrancisco.api.modules.rrhh.service.interfaces.AsistenciaService;
import com.sanfrancisco.api.modules.seguridad.security.UserPrincipal;
import com.sanfrancisco.api.shared.api.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Marcado self-service de asistencia: el empleado autenticado registra su propia
 * entrada/salida. El usuario se infiere del JWT, nunca se recibe por el cliente.
 */
@RestController
@RequestMapping("/api/v1/mi-asistencia")
public class MiAsistenciaController {

    private final AsistenciaService asistenciaService;

    public MiAsistenciaController(AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    @PostMapping("/marcar-entrada")
    public ResponseEntity<ApiResponse<AsistenciaResponse>> marcarEntrada(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(asistenciaService.marcarEntrada(userPrincipal.userId()),
                        "Entrada registrada"));
    }

    @PostMapping("/marcar-salida")
    public ApiResponse<AsistenciaResponse> marcarSalida(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ApiResponse.ok(asistenciaService.marcarSalida(userPrincipal.userId()),
                "Salida registrada");
    }

    @GetMapping
    public ApiResponse<List<AsistenciaResponse>> misAsistencias(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ApiResponse.ok(asistenciaService.misAsistencias(userPrincipal.userId()));
    }
}
