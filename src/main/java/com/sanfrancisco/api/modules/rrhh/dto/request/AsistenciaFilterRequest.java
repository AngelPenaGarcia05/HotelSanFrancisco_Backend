package com.sanfrancisco.api.modules.rrhh.dto.request;

import com.sanfrancisco.api.modules.rrhh.enums.TipoAsistencia;

import java.time.LocalDate;

public record AsistenciaFilterRequest(
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Integer usuarioId,
        TipoAsistencia tipo
) {
}
