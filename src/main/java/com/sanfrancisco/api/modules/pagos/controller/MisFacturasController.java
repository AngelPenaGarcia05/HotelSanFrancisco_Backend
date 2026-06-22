package com.sanfrancisco.api.modules.pagos.controller;

import com.sanfrancisco.api.modules.pagos.service.interfaces.MiFacturaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@Tag(name = "Mis Facturas", description = "Comprobantes de pago del cliente autenticado.")
@RestController
@RequestMapping("/api/v1/mis-facturas")
public class MisFacturasController {

    private final MiFacturaService miFacturaService;

    public MisFacturasController(MiFacturaService miFacturaService) {
        this.miFacturaService = miFacturaService;
    }

    @Operation(summary = "Ver comprobante de pago",
            description = "Devuelve el comprobante (HTML imprimible) de un pago del cliente autenticado. "
                    + "Se abre en el navegador y puede guardarse como PDF al imprimir.")
    @GetMapping(value = "/{pagoId}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<byte[]> verFactura(@PathVariable Integer pagoId) {
        String html = miFacturaService.generarFacturaHtml(pagoId);
        return ResponseEntity.ok()
                .contentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8))
                .body(html.getBytes(StandardCharsets.UTF_8));
    }
}
