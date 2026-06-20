package com.sanfrancisco.api.modules.rrhh.dto.request;

import com.sanfrancisco.api.modules.rrhh.enums.TipoAsistencia;

import java.time.LocalTime;

public record UpdateAsistenciaRequest(
        LocalTime horaIngreso,
        LocalTime horaEgreso,
        TipoAsistencia tipo,
        String observaciones
) {
}
