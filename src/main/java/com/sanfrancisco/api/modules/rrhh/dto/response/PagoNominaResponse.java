package com.sanfrancisco.api.modules.rrhh.dto.response;

import com.sanfrancisco.api.modules.rrhh.enums.EstadoNomina;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PagoNominaResponse(
        Integer pagoNominaId,
        String periodo,
        LocalDate fechaEmision,
        BigDecimal sueldoBase,
        BigDecimal totalBonos,
        BigDecimal totalDescuentos,
        BigDecimal montoNeto,
        EstadoNomina estado,
        Integer usuarioId,
        String usuarioNombreCompleto,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {
}
