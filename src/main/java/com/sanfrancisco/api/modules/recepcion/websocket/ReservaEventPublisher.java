package com.sanfrancisco.api.modules.recepcion.websocket;

import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.recepcion.websocket.dto.ReservaEventPayload;
import com.sanfrancisco.api.shared.websocket.WebSocketChannels;
import com.sanfrancisco.api.shared.websocket.WebSocketEvent;
import com.sanfrancisco.api.shared.websocket.WebSocketPublisher;
import org.springframework.stereotype.Component;

@Component
public class ReservaEventPublisher {

    public static final String EVENT_CREATED = "RESERVA_CREADA";
    public static final String EVENT_UPDATED = "RESERVA_ACTUALIZADA";
    public static final String EVENT_STATE_CHANGED = "RESERVA_CAMBIO_ESTADO";
    public static final String EVENT_DELETED = "RESERVA_ELIMINADA";

    private static final String ENTITY = "reserva";

    private final WebSocketPublisher publisher;

    public ReservaEventPublisher(WebSocketPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishCreated(Reserva reserva) {
        publish(EVENT_CREATED, reserva);
    }

    public void publishUpdated(Reserva reserva) {
        publish(EVENT_UPDATED, reserva);
    }

    public void publishStateChanged(Reserva reserva) {
        publish(EVENT_STATE_CHANGED, reserva);
    }

    public void publishDeleted(Integer reservaId, String codReserva) {
        ReservaEventPayload payload = new ReservaEventPayload(reservaId, codReserva, null, null, null, null);
        publisher.broadcast(WebSocketChannels.TOPIC_RESERVAS,
                WebSocketEvent.of(EVENT_DELETED, ENTITY, payload));
    }

    private void publish(String eventType, Reserva reserva) {
        ReservaEventPayload payload = new ReservaEventPayload(
                reserva.getReservaId(),
                reserva.getCodReserva(),
                reserva.getEstado(),
                reserva.getFechaInicio(),
                reserva.getFechaFin(),
                reserva.getMontoTotal()
        );
        publisher.broadcast(WebSocketChannels.TOPIC_RESERVAS,
                WebSocketEvent.of(eventType, ENTITY, payload));
    }
}
