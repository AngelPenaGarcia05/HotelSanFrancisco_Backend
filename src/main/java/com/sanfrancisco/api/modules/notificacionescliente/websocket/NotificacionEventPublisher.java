package com.sanfrancisco.api.modules.notificacionescliente.websocket;

import com.sanfrancisco.api.modules.notificacionescliente.entity.NotificacionHuesped;
import com.sanfrancisco.api.modules.notificacionescliente.websocket.dto.NotificacionEventPayload;
import com.sanfrancisco.api.shared.websocket.WebSocketEvent;
import com.sanfrancisco.api.shared.websocket.WebSocketPublisher;
import org.springframework.stereotype.Component;

/**
 * Publica notificaciones in-app del huésped en tiempo real.
 *
 * <p>A diferencia del resto de módulos (que hacen broadcast a un {@code /topic/**}),
 * las notificaciones son personales: se envían de forma dirigida al usuario
 * destinatario vía {@code convertAndSendToUser}. El cliente debe suscribirse a
 * {@code /user/queue/notificaciones} (Spring resuelve el prefijo {@code /user}
 * a la sesión del usuario autenticado). Así el título y mensaje nunca se
 * exponen a otros clientes conectados.
 */
@Component
public class NotificacionEventPublisher {

    public static final String EVENT_CREATED = "NOTIFICACION_CREADA";

    /** Destino relativo; Spring lo antepone con /user/ para el usuario destinatario. */
    public static final String QUEUE_NOTIFICACIONES = "/queue/notificaciones";

    private static final String ENTITY = "notificacion";

    private final WebSocketPublisher publisher;

    public NotificacionEventPublisher(WebSocketPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Notifica al usuario destinatario que se creó una notificación in-app.
     *
     * @param correo correo del usuario, que coincide con el nombre del Principal STOMP
     */
    public void publishCreated(String correo, NotificacionHuesped notificacion) {
        NotificacionEventPayload payload = new NotificacionEventPayload(
                notificacion.getNotificacionId(),
                notificacion.getTipo(),
                notificacion.getTitulo(),
                notificacion.getMensaje(),
                notificacion.getLeida(),
                notificacion.getFechaCreacion(),
                notificacion.getReferenciaId()
        );
        publisher.toUser(correo, QUEUE_NOTIFICACIONES,
                WebSocketEvent.of(EVENT_CREATED, ENTITY, payload));
    }
}
