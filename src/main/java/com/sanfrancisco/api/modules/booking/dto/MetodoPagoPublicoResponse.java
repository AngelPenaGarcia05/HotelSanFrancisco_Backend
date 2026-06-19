package com.sanfrancisco.api.modules.booking.dto;

public record MetodoPagoPublicoResponse(
        Integer metodoPagoId,
        String nombre,
        Boolean requiereComprobante
) {}
