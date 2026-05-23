package com.sanfrancisco.api.modules.seguridad.websocket;

import com.sanfrancisco.api.modules.seguridad.security.JwtService;
import com.sanfrancisco.api.modules.seguridad.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtChannelInterceptor.class);

    private final JwtService jwtService;

    public JwtChannelInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            StompCommand command = accessor.getCommand();

            if (StompCommand.CONNECT.equals(command)) {
                String token = null;

                String authHeader = accessor.getFirstNativeHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }

                if (token == null && accessor.getSessionAttributes() != null) {
                    token = (String) accessor.getSessionAttributes().get("access_token");
                }

                if (token != null && jwtService.validateToken(token)) {
                    Claims claims = jwtService.extractClaims(token);
                    String email = claims.getSubject();

                    if (email != null) {
                        Integer userId = claims.get("userId", Integer.class);
                        String role = claims.get("role", String.class);
                        List<?> permissionsObj = claims.get("permissions", List.class);

                        Collection<GrantedAuthority> authorities = new ArrayList<>();
                        if (role != null) {
                            String roleName = role.toUpperCase();
                            if (!roleName.startsWith("ROLE_")) {
                                roleName = "ROLE_" + roleName;
                            }
                            authorities.add(new SimpleGrantedAuthority(roleName));
                        }

                        if (permissionsObj != null) {
                            for (Object p : permissionsObj) {
                                if (p instanceof String) {
                                    authorities.add(new SimpleGrantedAuthority((String) p));
                                }
                            }
                        }

                        UserPrincipal principal = new UserPrincipal(userId, email, authorities);
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                authorities
                        );

                        accessor.setUser(authentication);
                        log.debug("WebSocket/STOMP CONNECT exitoso para el usuario: {}", email);
                    }
                } else {
                    log.warn("Rechazando conexión WebSocket/STOMP: JWT inválido o ausente");
                    throw new MessageDeliveryException("Acceso denegado - Credenciales inválidas para WebSocket");
                }
            } else if (StompCommand.SUBSCRIBE.equals(command)) {
                String destination = accessor.getDestination();
                Principal user = accessor.getUser();

                if (destination != null && destination.startsWith("/topic/seguridad")) {
                    if (user == null || !hasRole(user, "ROLE_ADMIN")) {
                        log.warn("Suscripción rechazada para el canal: {} - Permisos insuficientes", destination);
                        throw new MessageDeliveryException("No tiene permisos para suscribirse a este canal");
                    }
                }
            }
        }

        return message;
    }

    private boolean hasRole(Principal principal, String role) {
        if (principal instanceof UsernamePasswordAuthenticationToken authToken) {
            return authToken.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equalsIgnoreCase(role));
        }
        return false;
    }
}
