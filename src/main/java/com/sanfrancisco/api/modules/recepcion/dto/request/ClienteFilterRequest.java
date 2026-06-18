package com.sanfrancisco.api.modules.recepcion.dto.request;

import com.sanfrancisco.api.shared.enums.EstadoActivo;

public record ClienteFilterRequest(
        String nombre,
        String apellidoPaterno,
        String numeroDocumento,
        String nacionalidad,
        String correo,
        EstadoActivo estado
) {
}
