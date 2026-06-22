package com.sanfrancisco.api.modules.notificacionescliente.controller;

import com.sanfrancisco.api.modules.notificacionescliente.dto.response.NotificacionHuespedResponse;
import com.sanfrancisco.api.modules.notificacionescliente.service.interfaces.NotificacionClienteService;
import com.sanfrancisco.api.shared.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notificaciones Cliente",
        description = "Bandeja de notificaciones in-app del cliente autenticado (rol CLIENTE).")
@RestController
@RequestMapping("/api/v1/notificaciones")
public class NotificacionClienteController {

    private final NotificacionClienteService service;

    public NotificacionClienteController(NotificacionClienteService service) {
        this.service = service;
    }

    @Operation(summary = "Listar mis notificaciones",
            description = "Devuelve las notificaciones del cliente autenticado, ordenadas por fecha descendente.")
    @GetMapping
    public PageResponse<NotificacionHuespedResponse> listarMias(
            @PageableDefault(size = 100, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.from(service.getMias(pageable));
    }

    @Operation(summary = "Marcar todas como leídas",
            description = "Marca como leídas todas las notificaciones del cliente autenticado.")
    @PatchMapping("/leer-todas")
    public ResponseEntity<Void> leerTodas() {
        service.marcarTodasLeidas();
        return ResponseEntity.noContent().build();
    }
}
