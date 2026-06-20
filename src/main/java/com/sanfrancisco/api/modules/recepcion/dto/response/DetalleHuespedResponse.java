package com.sanfrancisco.api.modules.recepcion.dto.response;

public record DetalleHuespedResponse(
        Integer huespedId,
        String nombreCompleto,
        String numeroDocumento,
        String correo,
        String telefono,
        Boolean esPrincipal
) {
}
