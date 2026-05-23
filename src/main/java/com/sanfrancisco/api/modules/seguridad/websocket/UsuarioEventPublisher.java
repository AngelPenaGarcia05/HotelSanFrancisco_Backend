package com.sanfrancisco.api.modules.seguridad.websocket;

import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.websocket.dto.UsuarioEventPayload;
import com.sanfrancisco.api.shared.websocket.WebSocketEvent;
import com.sanfrancisco.api.shared.websocket.WebSocketPublisher;
import org.springframework.stereotype.Component;

@Component
public class UsuarioEventPublisher {

    public static final String EVENT_CREATED = "USUARIO_CREADO";
    public static final String EVENT_UPDATED = "USUARIO_ACTUALIZADO";
    public static final String EVENT_DELETED = "USUARIO_ELIMINADO";

    public static final String TOPIC_SEGURIDAD = "/topic/seguridad";
    private static final String ENTITY = "usuario";

    private final WebSocketPublisher publisher;

    public UsuarioEventPublisher(WebSocketPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishCreated(Usuario usuario) {
        publish(EVENT_CREATED, usuario);
    }

    public void publishUpdated(Usuario usuario) {
        publish(EVENT_UPDATED, usuario);
    }

    public void publishDeleted(Integer usuarioId, String correo) {
        UsuarioEventPayload payload = new UsuarioEventPayload(usuarioId, null, correo, null, null);
        publisher.broadcast(TOPIC_SEGURIDAD,
                WebSocketEvent.of(EVENT_DELETED, ENTITY, payload));
    }

    private void publish(String eventType, Usuario usuario) {
        String apellidoMaternoStr = usuario.getApellidoMaterno() != null ? " " + usuario.getApellidoMaterno() : "";
        String nombreCompleto = usuario.getNombre() + " " + usuario.getApellidoPaterno() + apellidoMaternoStr;

        UsuarioEventPayload payload = new UsuarioEventPayload(
                usuario.getUsuarioId(),
                nombreCompleto,
                usuario.getCorreo(),
                usuario.getEstado(),
                usuario.getRol() != null ? usuario.getRol().getNombre() : null
        );
        publisher.broadcast(TOPIC_SEGURIDAD,
                WebSocketEvent.of(eventType, ENTITY, payload));
    }
}
