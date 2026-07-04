package com.sanfrancisco.api.config;

import com.sanfrancisco.api.modules.seguridad.websocket.HttpHandshakeInterceptor;
import com.sanfrancisco.api.modules.seguridad.websocket.JwtChannelInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final HttpHandshakeInterceptor handshakeInterceptor;
    private final JwtChannelInterceptor channelInterceptor;

    // Mismos orígenes que CORS: con "*" cualquier página podía abrir el socket
    // con las cookies del usuario (SockJS las envía) y suscribirse a los topics.
    @Value("${app.cors.allowed-origins:http://localhost:4200,http://127.0.0.1:4200}")
    private String allowedOrigins;

    public WebSocketConfig(HttpHandshakeInterceptor handshakeInterceptor,
                           JwtChannelInterceptor channelInterceptor) {
        this.handshakeInterceptor = handshakeInterceptor;
        this.channelInterceptor = channelInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] origins = java.util.Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(o -> !o.isEmpty())
                .toArray(String[]::new);
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(origins)
                .addInterceptors(handshakeInterceptor)
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(channelInterceptor);
    }
}
