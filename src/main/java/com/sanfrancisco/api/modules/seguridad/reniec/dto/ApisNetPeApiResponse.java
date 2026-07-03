package com.sanfrancisco.api.modules.seguridad.reniec.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Estructura cruda de la respuesta del proveedor apis.net.pe (v2)
 * para la consulta de DNI: {@code GET /v2/reniec/dni?numero=...}
 * con header {@code Authorization: Bearer <token>}.
 * <p>
 * Respuesta exitosa típica:
 * <pre>{ "nombres": "...", "apellidoPaterno": "...", "apellidoMaterno": "...",
 *        "nombreCompleto": "...", "tipoDocumento": "1", "numeroDocumento": "...",
 *        "digitoVerificador": "1" }</pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApisNetPeApiResponse(
        String nombres,
        String apellidoPaterno,
        String apellidoMaterno,
        String nombreCompleto,
        String tipoDocumento,
        String numeroDocumento,
        String digitoVerificador,
        String message
) {}
