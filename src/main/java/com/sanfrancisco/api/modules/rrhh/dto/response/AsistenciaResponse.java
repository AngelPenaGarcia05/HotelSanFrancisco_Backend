package com.sanfrancisco.api.modules.rrhh.dto.response;

import com.sanfrancisco.api.modules.rrhh.enums.TipoAsistencia;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AsistenciaResponse(
        Integer asistenciaId,
        LocalDate fecha,
        LocalTime horaIngreso,
        LocalTime horaEgreso,
        BigDecimal horasTrabajadas,
        TipoAsistencia tipo,
        String observaciones,
        Integer usuarioId,
        String usuarioNombreCompleto,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {
}
