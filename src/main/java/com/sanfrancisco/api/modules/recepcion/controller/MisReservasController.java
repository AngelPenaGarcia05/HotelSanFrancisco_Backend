package com.sanfrancisco.api.modules.recepcion.controller;

import com.sanfrancisco.api.modules.recepcion.dto.request.CancelarReservaRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.CreateReservaRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.MisReservasCreateRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.CancelacionResponse;
import com.sanfrancisco.api.modules.recepcion.dto.response.ReservaResponse;
import com.sanfrancisco.api.modules.recepcion.service.interfaces.ReservaService;
import com.sanfrancisco.api.modules.seguridad.security.UserPrincipal;
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
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            Pageable pageable) {
        return ApiResponse.ok(
                PageResponse.from(reservaService.findByUsuarioId(userPrincipal.userId(), pageable))
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<ReservaResponse> detalle(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Integer id) {
        return ApiResponse.ok(
                reservaService.findPropiaById(id, userPrincipal.userId())
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReservaResponse>> crear(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody MisReservasCreateRequest request) {

        CreateReservaRequest fullRequest = new CreateReservaRequest(
                null,                       // codReserva lo genera el backend
                request.fechaInicio(),
                request.fechaFin(),
                request.nroAdultos(),
                request.nroNinos(),
                BigDecimal.ZERO,            // descuento: el cliente nunca aplica descuento
                null,                       // adelanto: lo deriva el backend de la modalidad
                null,                       // impuesto: lo recalcula el backend
                request.modalidadPago(),
                request.observaciones(),
                userPrincipal.userId(),
                null,
                request.habitaciones(),
                request.huespedes(),
                false
        );

        ReservaResponse response = reservaService.createParaCliente(fullRequest, userPrincipal.userId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Reserva creada exitosamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<CancelacionResponse>> cancelar(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Integer id,
            @Valid @RequestBody CancelarReservaRequest request) {

        CancelacionResponse response = reservaService.cancelarPropiaReserva(
                id, userPrincipal.userId(), request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Reserva cancelada exitosamente"));
    }
}
