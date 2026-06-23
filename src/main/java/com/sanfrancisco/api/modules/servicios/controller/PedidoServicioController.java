package com.sanfrancisco.api.modules.servicios.controller;

import com.sanfrancisco.api.modules.servicios.dto.request.RechazarPedidoServicioRequest;
import com.sanfrancisco.api.modules.servicios.dto.response.PedidoServicioResponse;
import com.sanfrancisco.api.modules.servicios.enums.EstadoPedidoServicio;
import com.sanfrancisco.api.modules.servicios.service.interfaces.PedidoServicioService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import com.sanfrancisco.api.shared.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/**
 * Gestión de pedidos de servicio para recepción/admin. Reutiliza los permisos
 * existentes del módulo de servicios: lectura con {@code servicio:read} y la
 * aprobación/rechazo (que genera o descarta el consumo) con {@code servicio:create}.
 */
@Tag(name = "Pedidos de Servicio", description = "Aprobación y rechazo de pedidos de servicio de los clientes.")
@RestController
@RequestMapping("/api/v1/pedidos-servicio")
public class PedidoServicioController {

    private final PedidoServicioService service;

    public PedidoServicioController(PedidoServicioService service) {
        this.service = service;
    }

    @Operation(summary = "Listar pedidos",
            description = "Lista paginada de pedidos de servicio. Filtra por estado si se envía el parámetro.")
    @GetMapping
    public ApiResponse<PageResponse<PedidoServicioResponse>> listar(
            @RequestParam(required = false) EstadoPedidoServicio estado,
            @PageableDefault(size = 20, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(service.buscar(estado, pageable)));
    }

    @Operation(summary = "Aprobar pedido",
            description = "Aprueba un pedido PENDIENTE y genera el consumo facturable (Servicio) en la estadía.")
    @PatchMapping("/{id}/aprobar")
    public ApiResponse<PedidoServicioResponse> aprobar(@PathVariable Integer id) {
        return ApiResponse.ok(service.aprobar(id), "Pedido aprobado y servicio registrado");
    }

    @Operation(summary = "Rechazar pedido",
            description = "Rechaza un pedido PENDIENTE indicando el motivo. No genera consumo.")
    @PatchMapping("/{id}/rechazar")
    public ApiResponse<PedidoServicioResponse> rechazar(
            @PathVariable Integer id,
            @Valid @RequestBody RechazarPedidoServicioRequest request) {
        return ApiResponse.ok(service.rechazar(id, request), "Pedido rechazado");
    }
}
