package com.sanfrancisco.api.modules.compras.websocket;

import com.sanfrancisco.api.modules.compras.entity.Compra;
import com.sanfrancisco.api.modules.compras.websocket.dto.CompraEventPayload;
import com.sanfrancisco.api.shared.websocket.WebSocketChannels;
import com.sanfrancisco.api.shared.websocket.WebSocketEvent;
import com.sanfrancisco.api.shared.websocket.WebSocketPublisher;
import org.springframework.stereotype.Component;

@Component
public class CompraEventPublisher {

    public static final String EVENT_CREATED = "COMPRA_CREADA";
    public static final String EVENT_UPDATED = "COMPRA_ACTUALIZADA";
    public static final String EVENT_STATE_CHANGED = "COMPRA_CAMBIO_ESTADO";
    public static final String EVENT_DELETED = "COMPRA_ELIMINADA";

    private static final String ENTITY = "compra";

    private final WebSocketPublisher publisher;

    public CompraEventPublisher(WebSocketPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishCreated(Compra compra) {
        publish(EVENT_CREATED, compra);
    }

    public void publishUpdated(Compra compra) {
        publish(EVENT_UPDATED, compra);
    }

    public void publishStateChanged(Compra compra) {
        publish(EVENT_STATE_CHANGED, compra);
    }

    public void publishDeleted(Integer compraId, String numeroFactura) {
        CompraEventPayload payload = new CompraEventPayload(compraId, numeroFactura, null, null, null, null);
        publisher.broadcast(WebSocketChannels.TOPIC_COMPRAS,
                WebSocketEvent.of(EVENT_DELETED, ENTITY, payload));
    }

    private void publish(String eventType, Compra compra) {
        CompraEventPayload payload = new CompraEventPayload(
                compra.getCompraId(),
                compra.getNumeroFactura(),
                compra.getEstado(),
                compra.getFechaCompra(),
                compra.getMontoTotal(),
                compra.getProveedor() != null ? compra.getProveedor().getProveedorId() : null
        );
        publisher.broadcast(WebSocketChannels.TOPIC_COMPRAS,
                WebSocketEvent.of(eventType, ENTITY, payload));
    }
}
