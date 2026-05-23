package com.sanfrancisco.api.shared.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketPublisher {

    private static final Logger log = LoggerFactory.getLogger(WebSocketPublisher.class);

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public <T> void broadcast(String destination, WebSocketEvent<T> event) {
        try {
            messagingTemplate.convertAndSend(destination, event);
        } catch (Exception ex) {
            log.warn("Falló publicación WebSocket a {}: {}", destination, ex.getMessage());
        }
    }

    public <T> void toUser(String username, String destination, WebSocketEvent<T> event) {
        try {
            messagingTemplate.convertAndSendToUser(username, destination, event);
        } catch (Exception ex) {
            log.warn("Falló publicación WebSocket a usuario {} en {}: {}", username, destination, ex.getMessage());
        }
    }
}
