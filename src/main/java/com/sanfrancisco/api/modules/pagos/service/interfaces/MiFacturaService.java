package com.sanfrancisco.api.modules.pagos.service.interfaces;

public interface MiFacturaService {

    /**
     * Genera el comprobante (HTML imprimible) de un pago del cliente autenticado.
     * Valida que el pago pertenezca al cliente antes de devolver el documento.
     */
    String generarFacturaHtml(Integer pagoId);
}
