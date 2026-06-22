package com.sanfrancisco.api.modules.notificacionescliente.dto.response;

import com.sanfrancisco.api.modules.notificacionescliente.enums.TipoNotificacionHuesped;

import java.time.LocalDateTime;

public record NotificacionHuespedResponse(
        Integer notificacionId,
        TipoNotificacionHuesped tipo,
        String titulo,
        String mensaje,
        Boolean leida,
        LocalDateTime fechaCreacion,
        Integer referenciaId
) {}
