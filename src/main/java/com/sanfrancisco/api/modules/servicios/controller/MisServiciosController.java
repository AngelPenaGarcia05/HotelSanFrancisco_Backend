package com.sanfrancisco.api.modules.servicios.controller;

import com.sanfrancisco.api.modules.servicios.dto.request.CreatePedidoServicioRequest;
import com.sanfrancisco.api.modules.servicios.dto.response.PedidoServicioResponse;
import com.sanfrancisco.api.modules.servicios.service.interfaces.PedidoServicioService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Mis Servicios", description = "Pedidos de servicio del cliente autenticado durante su estadía.")
@RestController
@RequestMapping("/api/v1/mis-servicios")
public class MisServiciosController {

    private final PedidoServicioService service;

    public MisServiciosController(PedidoServicioService service) {
        this.service = service;
    }

    @Operation(summary = "Pedir un servicio",
            description = "Registra un pedido de servicio (estado PENDIENTE) para la estadía activa del "
                    + "cliente. Responde error si el cliente no tiene una estancia con check-in realizado.")
    @PostMapping
    public ResponseEntity<ApiResponse<PedidoServicioResponse>> pedir(
            @Valid @RequestBody CreatePedidoServicioRequest request) {
        PedidoServicioResponse creado = service.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(creado, "Pedido de servicio registrado. Recepción lo revisará en breve."));
    }

    @Operation(summary = "Listar mis pedidos",
            description = "Devuelve los pedidos de servicio del cliente autenticado, del más reciente al más antiguo.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PedidoServicioResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(service.getMisPedidos()));
    }

    @Operation(summary = "Cancelar un pedido",
            description = "Cancela un pedido propio mientras siga PENDIENTE. Responde 404 si el pedido no "
                    + "existe o no pertenece al cliente.")
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ApiResponse<PedidoServicioResponse>> cancelar(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(service.cancelar(id), "Pedido cancelado"));
    }
}
