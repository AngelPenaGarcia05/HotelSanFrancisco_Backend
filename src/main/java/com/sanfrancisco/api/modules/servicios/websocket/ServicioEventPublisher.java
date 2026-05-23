package com.sanfrancisco.api.modules.servicios.websocket;

import com.sanfrancisco.api.modules.servicios.entity.Servicio;
import com.sanfrancisco.api.modules.servicios.websocket.dto.ServicioEventPayload;
import com.sanfrancisco.api.shared.websocket.WebSocketChannels;
import com.sanfrancisco.api.shared.websocket.WebSocketEvent;
import com.sanfrancisco.api.shared.websocket.WebSocketPublisher;
import org.springframework.stereotype.Component;

@Component
public class ServicioEventPublisher {

    public static final String EVENT_CREATED = "SERVICIO_CREADO";
    public static final String EVENT_UPDATED = "SERVICIO_ACTUALIZADO";
    public static final String EVENT_DELETED = "SERVICIO_ELIMINADO";

    private static final String ENTITY = "servicio";

    private final WebSocketPublisher publisher;

    public ServicioEventPublisher(WebSocketPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishCreated(Servicio servicio) {
        publish(EVENT_CREATED, servicio);
    }

    public void publishUpdated(Servicio servicio) {
        publish(EVENT_UPDATED, servicio);
    }

    public void publishDeleted(Integer servicioId) {
        ServicioEventPayload payload = new ServicioEventPayload(servicioId, null, null, null, null, null);
        publisher.broadcast(WebSocketChannels.TOPIC_SERVICIOS,
                WebSocketEvent.of(EVENT_DELETED, ENTITY, payload));
    }

    private void publish(String eventType, Servicio servicio) {
        ServicioEventPayload payload = new ServicioEventPayload(
                servicio.getServicioId(),
                servicio.getTipoServicio() != null ? servicio.getTipoServicio().getTipoServicioId() : null,
                servicio.getEstancia() != null ? servicio.getEstancia().getEstanciaId() : null,
                servicio.getCantidad(),
                servicio.getSubtotal(),
                servicio.getFechaConsumo()
        );
        publisher.broadcast(WebSocketChannels.TOPIC_SERVICIOS,
                WebSocketEvent.of(eventType, ENTITY, payload));
    }
}
