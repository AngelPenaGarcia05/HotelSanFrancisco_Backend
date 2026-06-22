package com.sanfrancisco.api.modules.notificacionescliente.service.interfaces;

import com.sanfrancisco.api.modules.notificacionescliente.dto.response.NotificacionHuespedResponse;
import com.sanfrancisco.api.modules.notificacionescliente.enums.TipoNotificacionHuesped;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificacionClienteService {

    /** Notificaciones del cliente autenticado, ordenadas por fecha descendente. */
    Page<NotificacionHuespedResponse> getMias(Pageable pageable);

    /** Marca todas las notificaciones del cliente autenticado como leídas. Devuelve cuántas se afectaron. */
    int marcarTodasLeidas();

    /**
     * Registra una notificación in-app para un usuario. Best-effort: cualquier error
     * se captura y registra, nunca interrumpe la operación de negocio que la dispara.
     */
    void registrar(Integer usuarioId, TipoNotificacionHuesped tipo, String titulo, String mensaje, Integer referenciaId);
}
