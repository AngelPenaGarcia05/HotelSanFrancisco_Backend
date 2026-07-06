package com.sanfrancisco.api.modules.notificacionescliente.websocket.dto;

import com.sanfrancisco.api.modules.notificacionescliente.enums.TipoNotificacionHuesped;

import java.time.LocalDateTime;

/**
 * Payload para eventos en tiempo real de notificaciones in-app del huésped.
 * Se entrega de forma dirigida al usuario destinatario (no es broadcast),
 * por lo que puede contener el título y mensaje sin exponerlos a otros clientes.
 */
public record NotificacionEventPayload(
        Integer notificacionId,
        TipoNotificacionHuesped tipo,
        String titulo,
        String mensaje,
        Boolean leida,
        LocalDateTime fechaCreacion,
        Integer referenciaId
) {
}
