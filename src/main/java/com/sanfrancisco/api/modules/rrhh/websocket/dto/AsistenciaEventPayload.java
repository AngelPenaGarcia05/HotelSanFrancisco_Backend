package com.sanfrancisco.api.modules.rrhh.websocket.dto;

import com.sanfrancisco.api.modules.rrhh.enums.TipoAsistencia;

import java.time.LocalDate;
import java.time.LocalTime;

public record AsistenciaEventPayload(
        Integer asistenciaId,
        LocalDate fecha,
        LocalTime horaIngreso,
        LocalTime horaEgreso,
        TipoAsistencia tipo,
        Integer usuarioId,
        String usuarioNombreCompleto
) {
}
