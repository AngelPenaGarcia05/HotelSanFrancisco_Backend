package com.sanfrancisco.api.modules.pagos.websocket;

import com.sanfrancisco.api.modules.pagos.entity.Pago;
import com.sanfrancisco.api.modules.pagos.websocket.dto.PagoEventPayload;
import com.sanfrancisco.api.shared.websocket.WebSocketChannels;
import com.sanfrancisco.api.shared.websocket.WebSocketEvent;
import com.sanfrancisco.api.shared.websocket.WebSocketPublisher;
import org.springframework.stereotype.Component;

@Component
public class PagoEventPublisher {

    public static final String EVENT_CREATED = "PAGO_CREADO";
    public static final String EVENT_UPDATED = "PAGO_ACTUALIZADO";
    public static final String EVENT_DELETED = "PAGO_ELIMINADO";

    private static final String ENTITY = "pago";

    private final WebSocketPublisher publisher;

    public PagoEventPublisher(WebSocketPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishCreated(Pago pago) {
        publish(EVENT_CREATED, pago);
    }

    public void publishUpdated(Pago pago) {
        publish(EVENT_UPDATED, pago);
    }

    public void publishDeleted(Integer pagoId) {
        PagoEventPayload payload = new PagoEventPayload(pagoId, null, null, null, null, null, null);
        publisher.broadcast(WebSocketChannels.TOPIC_PAGOS,
                WebSocketEvent.of(EVENT_DELETED, ENTITY, payload));
    }

    private void publish(String eventType, Pago pago) {
        PagoEventPayload payload = new PagoEventPayload(
                pago.getPagoId(),
                pago.getTipoPago(),
                pago.getMonto(),
                pago.getFecha(),
                pago.getMetodoPago() != null ? pago.getMetodoPago().getMetodoPagoId() : null,
                pago.getVenta() != null ? pago.getVenta().getVentaId() : null,
                pago.getReserva() != null ? pago.getReserva().getReservaId() : null
        );
        publisher.broadcast(WebSocketChannels.TOPIC_PAGOS,
                WebSocketEvent.of(eventType, ENTITY, payload));
    }
}
