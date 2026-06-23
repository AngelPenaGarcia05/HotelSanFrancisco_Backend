package com.sanfrancisco.api.modules.servicios.controller;

import com.sanfrancisco.api.modules.servicios.dto.response.TipoServicioResponse;
import com.sanfrancisco.api.modules.servicios.service.interfaces.TipoServicioService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Catálogo de servicios visible para el cliente autenticado (rol CLIENTE).
 * <p>
 * Espejo del patrón de {@code /api/v1/mis-pagos}: el cliente no posee el permiso
 * de administración {@code tipo-servicio:read}, por lo que se expone un endpoint
 * de solo lectura protegido con {@code servicio-catalogo:read}. Devuelve
 * únicamente los servicios en estado {@code ACTIVO}.
 * </p>
 */
@Tag(name = "Catálogo de Servicios", description = "Servicios disponibles del hotel para el cliente autenticado.")
@RestController
@RequestMapping("/api/v1/servicios-catalogo")
public class ServicioCatalogoController {

    private final TipoServicioService tipoServicioService;

    public ServicioCatalogoController(TipoServicioService tipoServicioService) {
        this.tipoServicioService = tipoServicioService;
    }

    @Operation(summary = "Listar servicios disponibles",
            description = "Devuelve los servicios del hotel en estado ACTIVO. Pensado para el panel del cliente.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TipoServicioResponse>>> listarCatalogo() {
        return ResponseEntity.ok(ApiResponse.ok(tipoServicioService.findByEstado(EstadoActivo.ACTIVO)));
    }
}
