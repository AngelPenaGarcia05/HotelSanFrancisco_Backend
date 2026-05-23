package com.sanfrancisco.api.modules.operaciones.websocket;

import com.sanfrancisco.api.modules.operaciones.entity.Incidencia;
import com.sanfrancisco.api.modules.operaciones.websocket.dto.IncidenciaEventPayload;
import com.sanfrancisco.api.shared.websocket.WebSocketChannels;
import com.sanfrancisco.api.shared.websocket.WebSocketEvent;
import com.sanfrancisco.api.shared.websocket.WebSocketPublisher;
import org.springframework.stereotype.Component;

@Component
public class IncidenciaEventPublisher {

    public static final String EVENT_CREATED = "INCIDENCIA_CREADA";
    public static final String EVENT_UPDATED = "INCIDENCIA_ACTUALIZADA";
    public static final String EVENT_STATE_CHANGED = "INCIDENCIA_CAMBIO_ESTADO";
    public static final String EVENT_DELETED = "INCIDENCIA_ELIMINADA";

    private static final String ENTITY = "incidencia";

    private final WebSocketPublisher publisher;

    public IncidenciaEventPublisher(WebSocketPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishCreated(Incidencia incidencia) {
        publish(EVENT_CREATED, incidencia);
    }

    public void publishUpdated(Incidencia incidencia) {
        publish(EVENT_UPDATED, incidencia);
    }

    public void publishStateChanged(Incidencia incidencia) {
        publish(EVENT_STATE_CHANGED, incidencia);
    }

    public void publishDeleted(Integer incidenciaId) {
        IncidenciaEventPayload payload = new IncidenciaEventPayload(incidenciaId, null, null, null, null, null);
        publisher.broadcast(WebSocketChannels.TOPIC_INCIDENCIAS,
                WebSocketEvent.of(EVENT_DELETED, ENTITY, payload));
    }

    private void publish(String eventType, Incidencia incidencia) {
        IncidenciaEventPayload payload = new IncidenciaEventPayload(
                incidencia.getIncidenciaId(),
                incidencia.getEstado(),
                incidencia.getPrioridad(),
                incidencia.getFechaReporte(),
                incidencia.getUsuario() != null ? incidencia.getUsuario().getUsuarioId() : null,
                incidencia.getReservaHabitacion() != null ? incidencia.getReservaHabitacion().getReservaHabitacionId() : null
        );
        publisher.broadcast(WebSocketChannels.TOPIC_INCIDENCIAS,
                WebSocketEvent.of(eventType, ENTITY, payload));
    }
}
