package com.sanfrancisco.api.modules.ventas.websocket;

import com.sanfrancisco.api.modules.ventas.entity.Venta;
import com.sanfrancisco.api.modules.ventas.websocket.dto.VentaEventPayload;
import com.sanfrancisco.api.shared.websocket.WebSocketChannels;
import com.sanfrancisco.api.shared.websocket.WebSocketEvent;
import com.sanfrancisco.api.shared.websocket.WebSocketPublisher;
import org.springframework.stereotype.Component;

@Component
public class VentaEventPublisher {

    public static final String EVENT_CREATED = "VENTA_CREADA";
    public static final String EVENT_UPDATED = "VENTA_ACTUALIZADA";
    public static final String EVENT_STATE_CHANGED = "VENTA_CAMBIO_ESTADO";
    public static final String EVENT_DELETED = "VENTA_ELIMINADA";

    private static final String ENTITY = "venta";

    private final WebSocketPublisher publisher;

    public VentaEventPublisher(WebSocketPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishCreated(Venta venta) {
        publish(EVENT_CREATED, venta);
    }

    public void publishUpdated(Venta venta) {
        publish(EVENT_UPDATED, venta);
    }

    public void publishStateChanged(Venta venta) {
        publish(EVENT_STATE_CHANGED, venta);
    }

    public void publishDeleted(Integer ventaId, String codigoVenta) {
        VentaEventPayload payload = new VentaEventPayload(ventaId, codigoVenta, null, null, null, null);
        publisher.broadcast(WebSocketChannels.TOPIC_VENTAS,
                WebSocketEvent.of(EVENT_DELETED, ENTITY, payload));
    }

    private void publish(String eventType, Venta venta) {
        VentaEventPayload payload = new VentaEventPayload(
                venta.getVentaId(),
                venta.getCodigoVenta(),
                venta.getEstado(),
                venta.getTipoVenta(),
                venta.getMontoTotal(),
                venta.getFechaVenta()
        );
        publisher.broadcast(WebSocketChannels.TOPIC_VENTAS,
                WebSocketEvent.of(eventType, ENTITY, payload));
    }
}
