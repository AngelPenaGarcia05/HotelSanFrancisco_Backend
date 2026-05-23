package com.sanfrancisco.api.modules.rrhh.websocket;

import com.sanfrancisco.api.modules.rrhh.entity.PagoNomina;
import com.sanfrancisco.api.modules.rrhh.websocket.dto.PagoNominaEventPayload;
import com.sanfrancisco.api.shared.websocket.WebSocketChannels;
import com.sanfrancisco.api.shared.websocket.WebSocketEvent;
import com.sanfrancisco.api.shared.websocket.WebSocketPublisher;
import org.springframework.stereotype.Component;

@Component
public class PagoNominaEventPublisher {

    public static final String EVENT_CREATED = "NOMINA_CREADA";
    public static final String EVENT_STATE_CHANGED = "NOMINA_CAMBIO_ESTADO";

    private static final String ENTITY = "nomina";

    private final WebSocketPublisher publisher;

    public PagoNominaEventPublisher(WebSocketPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishCreated(PagoNomina pago) {
        publish(EVENT_CREATED, pago);
    }

    public void publishStateChanged(PagoNomina pago) {
        publish(EVENT_STATE_CHANGED, pago);
    }

    private void publish(String eventType, PagoNomina pago) {
        String nombreCompleto = pago.getUsuario() != null ? 
            pago.getUsuario().getNombre() + " " + pago.getUsuario().getApellidoPaterno() : null;

        PagoNominaEventPayload payload = new PagoNominaEventPayload(
                pago.getPagoNominaId(),
                pago.getPeriodo(),
                pago.getFechaEmision(),
                pago.getMontoNeto(),
                pago.getEstado(),
                pago.getUsuario() != null ? pago.getUsuario().getUsuarioId() : null,
                nombreCompleto
        );
        publisher.broadcast(WebSocketChannels.TOPIC_NOMINA,
                WebSocketEvent.of(eventType, ENTITY, payload));
    }
}
