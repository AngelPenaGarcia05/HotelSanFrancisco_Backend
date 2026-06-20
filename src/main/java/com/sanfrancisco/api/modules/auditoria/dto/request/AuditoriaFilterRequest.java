package com.sanfrancisco.api.modules.auditoria.dto.request;

import com.sanfrancisco.api.modules.auditoria.enums.ResultadoAuditoria;

import java.time.LocalDate;

/**
 * Filtros opcionales para la consulta paginada de auditoría.
 * Todos los campos son opcionales; los nulos no aplican restricción.
 */
public record AuditoriaFilterRequest(
        Integer usuarioId,
        String usuarioCorreo,
        String accion,
        String modulo,
        ResultadoAuditoria resultado,
        LocalDate fechaDesde,
        LocalDate fechaHasta
) {}
