package com.sanfrancisco.api.modules.recepcion.controller;

import com.sanfrancisco.api.modules.recepcion.dto.request.ReservaOnlineRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.ReservaResponse;
import com.sanfrancisco.api.modules.recepcion.service.interfaces.ReservaOnlineService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/reservas")
public class ReservaOnlineController {

    private final ReservaOnlineService reservaOnlineService;

    public ReservaOnlineController(ReservaOnlineService reservaOnlineService) {
        this.reservaOnlineService = reservaOnlineService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReservaResponse>> crear(@Valid @RequestBody ReservaOnlineRequest request) {
        ReservaResponse response = reservaOnlineService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Reserva online creada exitosamente. Código: " + response.codReserva()));
    }
}
