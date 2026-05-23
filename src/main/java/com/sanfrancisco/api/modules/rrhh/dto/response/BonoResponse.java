package com.sanfrancisco.api.modules.rrhh.dto.response;

import com.sanfrancisco.api.modules.rrhh.enums.EstadoBono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BonoResponse(
        Integer bonoId,
        BigDecimal monto,
        String motivo,
        LocalDate fechaAsignacion,
        EstadoBono estado,
        Integer usuarioId,
        String usuarioNombreCompleto,
        Integer pagoNominaId,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {
}
