package com.sanfrancisco.api.modules.rrhh.websocket.dto;

import com.sanfrancisco.api.modules.rrhh.enums.EstadoNomina;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PagoNominaEventPayload(
        Integer pagoNominaId,
        String periodo,
        LocalDate fechaEmision,
        BigDecimal montoNeto,
        EstadoNomina estado,
        Integer usuarioId,
        String usuarioNombreCompleto
) {
}
