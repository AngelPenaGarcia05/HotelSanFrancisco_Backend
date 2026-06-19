package com.sanfrancisco.api.modules.seguridad.dto.response;

/**
 * Datos de identidad expuestos al cliente tras una consulta de DNI a RENIEC.
 * Sirve para autocompletar el formulario de registro.
 */
public record ReniecConsultaResponse(
        String dni,
        String nombres,
        String apellidoPaterno,
        String apellidoMaterno,
        String nombreCompleto
) {}
