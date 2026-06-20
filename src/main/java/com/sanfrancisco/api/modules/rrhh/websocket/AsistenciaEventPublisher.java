package com.sanfrancisco.api.modules.rrhh.websocket;

import com.sanfrancisco.api.modules.rrhh.entity.Asistencia;
import com.sanfrancisco.api.modules.rrhh.websocket.dto.AsistenciaEventPayload;
import com.sanfrancisco.api.shared.websocket.WebSocketChannels;
import com.sanfrancisco.api.shared.websocket.WebSocketEvent;
import com.sanfrancisco.api.shared.websocket.WebSocketPublisher;
import org.springframework.stereotype.Component;

@Component
public class AsistenciaEventPublisher {

    public static final String EVENT_CREATED = "ASISTENCIA_CREADA";
    public static final String EVENT_UPDATED = "ASISTENCIA_ACTUALIZADA";

    private static final String ENTITY = "asistencia";

    private final WebSocketPublisher publisher;

    public AsistenciaEventPublisher(WebSocketPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishCreated(Asistencia asistencia) {
        publish(EVENT_CREATED, asistencia);
    }

    public void publishUpdated(Asistencia asistencia) {
        publish(EVENT_UPDATED, asistencia);
    }

    private void publish(String eventType, Asistencia asistencia) {
        String nombreCompleto = asistencia.getUsuario() != null ? 
            asistencia.getUsuario().getNombre() + " " + asistencia.getUsuario().getApellidoPaterno() : null;

        AsistenciaEventPayload payload = new AsistenciaEventPayload(
                asistencia.getAsistenciaId(),
                asistencia.getFecha(),
                asistencia.getHoraIngreso(),
                asistencia.getHoraEgreso(),
                asistencia.getTipo(),
                asistencia.getUsuario() != null ? asistencia.getUsuario().getUsuarioId() : null,
                nombreCompleto
        );
        publisher.broadcast(WebSocketChannels.TOPIC_ASISTENCIA,
                WebSocketEvent.of(eventType, ENTITY, payload));
    }
}
