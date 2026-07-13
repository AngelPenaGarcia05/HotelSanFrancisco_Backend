package com.sanfrancisco.api.modules.reportes.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Reporte de ausentismo y puntualidad: totales del período y desglose por
 * empleado (normales, tardanzas, faltas, permisos, % de puntualidad y horas
 * trabajadas). Acceso restringido a asistencia:read (ADMIN/RRHH).
 */
public record AttendanceReportResponse(
        long totalRegistros,
        long normales,
        long tardanzas,
        long faltasJustificadas,
        long faltasInjustificadas,
        long permisos,
        BigDecimal puntualidad,
        BigDecimal horasTrabajadas,
        List<AttendanceByEmployee> porEmpleado
) {

    public record AttendanceByEmployee(
            String empleado,
            long registros,
            long normales,
            long tardanzas,
            long faltasJustificadas,
            long faltasInjustificadas,
            long permisos,
            BigDecimal puntualidad,
            BigDecimal horasTrabajadas
    ) {
    }
}
