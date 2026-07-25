package com.sanfrancisco.api.modules.pagos.controller;

import com.sanfrancisco.api.modules.pagos.service.interfaces.MiFacturaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Mis Facturas", description = "Comprobantes de pago del cliente autenticado.")
@RestController
@RequestMapping("/api/v1/mis-facturas")
public class MisFacturasController {

    private final MiFacturaService miFacturaService;

    public MisFacturasController(MiFacturaService miFacturaService) {
        this.miFacturaService = miFacturaService;
    }

    @Operation(summary = "Descargar comprobante de pago (PDF)",
            description = "Devuelve el comprobante de pago en formato PDF para un pago del cliente autenticado.")
    @GetMapping(value = "/{pagoId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> verFactura(@PathVariable Integer pagoId) {
        byte[] pdf = miFacturaService.generarFacturaPdf(pagoId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "comprobante-" + pagoId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
}
