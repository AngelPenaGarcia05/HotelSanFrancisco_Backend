package com.sanfrancisco.api.modules.seguridad.reniec.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Estructura cruda de la respuesta del proveedor apisperu.com
 * para la consulta de DNI: {@code GET /api/v1/dni/{dni}?token=...}.
 * <p>
 * Respuesta exitosa típica:
 * <pre>{ "dni": "...", "nombres": "...", "apellidoPaterno": "...",
 *        "apellidoMaterno": "...", "codVerifica": "1" }</pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReniecApiResponse(
        String dni,
        String nombres,
        String apellidoPaterno,
        String apellidoMaterno,
        String codVerifica,
        String message
) {}
