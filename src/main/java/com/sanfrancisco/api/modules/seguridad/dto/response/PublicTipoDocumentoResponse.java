package com.sanfrancisco.api.modules.seguridad.dto.response;

/**
 * Proyección reducida de TipoDocumento expuesta al formulario público de registro.
 * Sólo entrega los campos necesarios para poblar el select.
 */
public record PublicTipoDocumentoResponse(
        Integer tipoDocumentoId,
        String acronimo,
        String nombre
) {
}
