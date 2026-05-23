package com.sanfrancisco.api.modules.rrhh.dto.request;

import com.sanfrancisco.api.modules.rrhh.enums.EstadoNomina;

public record PagoNominaFilterRequest(
        String periodo,
        Integer usuarioId,
        EstadoNomina estado
) {
}
