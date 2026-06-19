package com.sanfrancisco.api.modules.auditoria.dto.response;

import com.sanfrancisco.api.modules.auditoria.enums.ResultadoAuditoria;

import java.time.LocalDateTime;

public record RegistroAuditoriaResponse(
        Integer registroId,
        Integer usuarioId,
        String usuarioCorreo,
        String accion,
        String modulo,
        String descripcion,
        String metodoHttp,
        String ruta,
        String ipOrigen,
        ResultadoAuditoria resultado,
        String detalleError,
        LocalDateTime fecha
) {}
