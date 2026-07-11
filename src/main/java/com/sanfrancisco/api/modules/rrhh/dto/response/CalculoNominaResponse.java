package com.sanfrancisco.api.modules.rrhh.dto.response;

import java.math.BigDecimal;

/**
 * Preview del cálculo de nómina derivado de la asistencia del periodo.
 * No persiste nada: RRHH revisa el desglose y confirma con POST /pagos-nomina.
 */
public record CalculoNominaResponse(
        Integer usuarioId,
        String usuarioNombreCompleto,
        String periodo,
        BigDecimal sueldoBase,
        // Base de cálculo
        int diasLaborables,
        BigDecimal horasEsperadas,
        BigDecimal horasReales,
        BigDecimal tarifaHora,
        // Incidencias derivadas de la asistencia
        long faltasInjustificadas,
        long tardanzas,
        // Desglose de descuentos
        BigDecimal descuentoFaltas,
        BigDecimal descuentoTardanzas,
        BigDecimal totalDescuentos,
        // Bonos activos no liquidados
        BigDecimal totalBonos,
        // Resultado
        BigDecimal montoNeto
) {
}
