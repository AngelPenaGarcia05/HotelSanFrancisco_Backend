package com.sanfrancisco.api.modules.inventario.websocket;

import com.sanfrancisco.api.modules.inventario.entity.Producto;
import com.sanfrancisco.api.modules.inventario.websocket.dto.ProductoEventPayload;
import com.sanfrancisco.api.shared.websocket.WebSocketChannels;
import com.sanfrancisco.api.shared.websocket.WebSocketEvent;
import com.sanfrancisco.api.shared.websocket.WebSocketPublisher;
import org.springframework.stereotype.Component;

@Component
public class ProductoEventPublisher {

    public static final String EVENT_CREATED = "PRODUCTO_CREADO";
    public static final String EVENT_UPDATED = "PRODUCTO_ACTUALIZADO";
    public static final String EVENT_STOCK_CHANGED = "PRODUCTO_STOCK_CAMBIADO";
    public static final String EVENT_STOCK_CRITICO = "PRODUCTO_STOCK_CRITICO";
    public static final String EVENT_DELETED = "PRODUCTO_ELIMINADO";

    private static final String ENTITY = "producto";

    private final WebSocketPublisher publisher;

    public ProductoEventPublisher(WebSocketPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishCreated(Producto producto) {
        publish(EVENT_CREATED, producto);
    }

    public void publishUpdated(Producto producto) {
        publish(EVENT_UPDATED, producto);
    }

    public void publishStockChanged(Producto producto) {
        publish(EVENT_STOCK_CHANGED, producto);
        if (esStockCritico(producto)) {
            publish(EVENT_STOCK_CRITICO, producto);
        }
    }

    public void publishDeleted(Integer productoId, String nombre) {
        ProductoEventPayload payload = new ProductoEventPayload(productoId, nombre, null, null, null);
        publisher.broadcast(WebSocketChannels.TOPIC_INVENTARIO,
                WebSocketEvent.of(EVENT_DELETED, ENTITY, payload));
    }

    private void publish(String eventType, Producto producto) {
        ProductoEventPayload payload = new ProductoEventPayload(
                producto.getProductoId(),
                producto.getNombre(),
                producto.getStockActual(),
                producto.getStockMinimo(),
                producto.getEstado()
        );
        publisher.broadcast(WebSocketChannels.TOPIC_INVENTARIO,
                WebSocketEvent.of(eventType, ENTITY, payload));
    }

    private boolean esStockCritico(Producto producto) {
        return producto.getStockActual() != null
                && producto.getStockMinimo() != null
                && producto.getStockActual().compareTo(producto.getStockMinimo()) <= 0;
    }
}
