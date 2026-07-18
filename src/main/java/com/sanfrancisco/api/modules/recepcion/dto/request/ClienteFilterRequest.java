package com.sanfrancisco.api.modules.recepcion.dto.request;

import com.sanfrancisco.api.shared.enums.EstadoActivo;

public record ClienteFilterRequest(
        /**
         * Término libre: busca por nombre, apellidos o número de documento (OR).
         * Es el filtro que usan los autocompletes (modal de reservas).
         */
        String q,
        String nombre,
        String apellidoPaterno,
        String numeroDocumento,
        String nacionalidad,
        String correo,
        EstadoActivo estado
) {
}
