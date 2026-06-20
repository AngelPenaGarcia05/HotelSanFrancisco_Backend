package com.sanfrancisco.api.modules.seguridad.websocket;

import com.sanfrancisco.api.modules.seguridad.entity.Rol;
import com.sanfrancisco.api.modules.seguridad.websocket.dto.RolEventPayload;
import com.sanfrancisco.api.shared.websocket.WebSocketEvent;
import com.sanfrancisco.api.shared.websocket.WebSocketPublisher;
import org.springframework.stereotype.Component;

@Component
public class RolEventPublisher {

    public static final String EVENT_CREATED = "ROL_CREADO";
    public static final String EVENT_UPDATED = "ROL_ACTUALIZADO";
    public static final String EVENT_DELETED = "ROL_ELIMINADO";

    public static final String TOPIC_SEGURIDAD = "/topic/seguridad";
    private static final String ENTITY = "rol";

    private final WebSocketPublisher publisher;

    public RolEventPublisher(WebSocketPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishCreated(Rol rol) {
        publish(EVENT_CREATED, rol);
    }

    public void publishUpdated(Rol rol) {
        publish(EVENT_UPDATED, rol);
    }

    public void publishDeleted(Integer rolId, String nombre) {
        RolEventPayload payload = new RolEventPayload(rolId, nombre, null);
        publisher.broadcast(TOPIC_SEGURIDAD,
                WebSocketEvent.of(EVENT_DELETED, ENTITY, payload));
    }

    private void publish(String eventType, Rol rol) {
        RolEventPayload payload = new RolEventPayload(
                rol.getRolId(),
                rol.getNombre(),
                rol.getEstado()
        );
        publisher.broadcast(TOPIC_SEGURIDAD,
                WebSocketEvent.of(eventType, ENTITY, payload));
    }
}
