package com.sanfrancisco.api.modules.seguridad.dto.response;

import com.sanfrancisco.api.shared.enums.EstadoActivo;
import java.time.LocalDateTime;

public record TipoDocumentoResponse(
        Integer tipoDocumentoId,
        String acronimo,
        String nombre,
        EstadoActivo estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {
}
