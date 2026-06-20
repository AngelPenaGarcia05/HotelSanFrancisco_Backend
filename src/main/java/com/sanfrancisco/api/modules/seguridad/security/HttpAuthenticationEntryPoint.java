package com.sanfrancisco.api.modules.seguridad.security;

import tools.jackson.databind.ObjectMapper;
import com.sanfrancisco.api.shared.api.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class HttpAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public HttpAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        ErrorResponse errorResponse = new ErrorResponse(
                false,
                "UNAUTHORIZED",
                "Acceso no autorizado - Debe iniciar sesión",
                request.getRequestURI(),
                null,
                null,
                Instant.now()
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
