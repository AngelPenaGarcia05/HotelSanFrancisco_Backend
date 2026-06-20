package com.sanfrancisco.api.modules.recepcion.controller;

import com.sanfrancisco.api.modules.recepcion.dto.request.CancelarReservaRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.CreateReservaRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.MisReservasCreateRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.CancelacionResponse;
import com.sanfrancisco.api.modules.recepcion.dto.response.ReservaResponse;
import com.sanfrancisco.api.modules.recepcion.service.interfaces.ReservaService;
import com.sanfrancisco.api.modules.seguridad.security.CustomUserDetails;
import com.sanfrancisco.api.shared.api.ApiResponse;
import com.sanfrancisco.api.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/mis-reservas")
public class MisReservasController {

    private final ReservaService reservaService;

    public MisReservasController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    public ApiResponse<PageResponse<ReservaResponse>> listarMisReservas(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {
        return ApiResponse.ok(
                PageResponse.from(reservaService.findByUsuarioId(userDetails.getUserId(), pageable))
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReservaResponse>> crear(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MisReservasCreateRequest request) {

        // Construir el request completo forzando el usuarioId del token
        CreateReservaRequest fullRequest = new CreateReservaRequest(
                request.codReserva(),
                request.fechaInicio(),
                request.fechaFin(),
                request.nroAdultos(),
                request.nroNinos(),
                BigDecimal.ZERO,                                                 // descuento = 0 para clientes
                request.adelanto() != null ? request.adelanto() : BigDecimal.ZERO,
                BigDecimal.ZERO,                                                 // impuesto = 0 para clientes
                request.observaciones(),
                userDetails.getUserId(),                                         // forzado desde JWT
                null,                                                            // canalId = directo
                request.habitaciones(),
                request.huespedes(),
                false                                                            // forzar = false
        );

        ReservaResponse response = reservaService.create(fullRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Reserva creada exitosamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<CancelacionResponse>> cancelar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id,
            @Valid @RequestBody CancelarReservaRequest request) {

        CancelacionResponse response = reservaService.cancelarPropiaReserva(
                id, userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Reserva cancelada exitosamente"));
    }
}
