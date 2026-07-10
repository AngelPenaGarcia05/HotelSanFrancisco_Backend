package com.sanfrancisco.api.modules.recepcion.dto.response;

public record DetalleHuespedResponse(
        Integer huespedId,
        String nombreCompleto,
        // Campos partidos: permiten pre-llenar fielmente el form de edición de
        // acompañantes (el reemplazo total del PATCH exige nombre/apellidoPaterno).
        String nombre,
        String apellidoPaterno,
        String apellidoMaterno,
        String numeroDocumento,
        String nacionalidad,
        String correo,
        String telefono,
        Boolean esPrincipal
) {
}
