package com.sanfrancisco.api.modules.auditoria.service;

import com.sanfrancisco.api.modules.auditoria.enums.ResultadoAuditoria;

/**
 * Datos que el aspecto entrega al servicio de auditoría para persistir un registro.
 */
public record AuditoriaCommand(
        Integer usuarioId,
        String usuarioCorreo,
        String accion,
        String modulo,
        String descripcion,
        String metodoHttp,
        String ruta,
        String ipOrigen,
        ResultadoAuditoria resultado,
        String detalleError
) {}
