package com.sanfrancisco.api.modules.pagos.controller;

import com.sanfrancisco.api.modules.pagos.dto.response.MiPagoResponse;
import com.sanfrancisco.api.modules.pagos.service.interfaces.MisPagosService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Mis Pagos", description = "Pagos y saldos del cliente autenticado.")
@RestController
@RequestMapping("/api/v1/mis-pagos")
public class MisPagosController {

    private final MisPagosService misPagosService;

    public MisPagosController(MisPagosService misPagosService) {
        this.misPagosService = misPagosService;
    }

    @Operation(summary = "Listar mis pagos",
            description = "Devuelve los pagos realizados y los saldos pendientes del cliente autenticado, "
                    + "ordenados por fecha descendente.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<MiPagoResponse>>> listarMisPagos() {
        return ResponseEntity.ok(ApiResponse.ok(misPagosService.getMisPagos()));
    }
}
