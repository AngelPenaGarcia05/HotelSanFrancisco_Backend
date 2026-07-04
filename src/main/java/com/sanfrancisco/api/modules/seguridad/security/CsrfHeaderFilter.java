package com.sanfrancisco.api.modules.seguridad.security;

import com.sanfrancisco.api.shared.api.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;

/**
 * Protección CSRF para el escenario de producción con frontend en otro dominio
 * (cookies con SameSite=None): exige el header {@code X-Requested-With} en toda
 * mutación autenticada por cookie. Un formulario cross-site puede enviar la
 * cookie, pero no puede añadir un header custom sin pasar el preflight de CORS,
 * que ya restringe los orígenes.
 * <p>
 * Solo aplica cuando la autenticación viaja en la cookie {@code access_token};
 * los requests con {@code Authorization: Bearer} no son vulnerables a CSRF y
 * los endpoints públicos (booking, login, registro) quedan fuera porque no
 * dependen de credenciales de ambiente para autorizar.
 * <p>
 * Desactivado por defecto ({@code APP_CSRF_HEADER_ENABLED=false}): activarlo
 * requiere que el frontend envíe {@code X-Requested-With: XMLHttpRequest} en
 * cada request (interceptor HTTP de Angular).
 */
@Component
public class CsrfHeaderFilter extends OncePerRequestFilter {

    public static final String CSRF_HEADER = "X-Requested-With";
    public static final String CSRF_HEADER_VALUE = "XMLHttpRequest";

    private static final Set<String> METODOS_MUTACION = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final ObjectMapper objectMapper;

    @Value("${app.security.csrf-header-enabled:false}")
    private boolean enabled;

    public CsrfHeaderFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (!enabled || !METODOS_MUTACION.contains(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean autenticaPorCookie = tieneCookie(request, JwtService.ACCESS_TOKEN_COOKIE)
                && noUsaBearer(request);

        if (autenticaPorCookie && !CSRF_HEADER_VALUE.equals(request.getHeader(CSRF_HEADER))) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            ErrorResponse error = new ErrorResponse(
                    false,
                    "CSRF_HEADER_MISSING",
                    "Falta el header " + CSRF_HEADER + " requerido para operaciones de escritura",
                    request.getRequestURI(),
                    null,
                    null,
                    Instant.now()
            );
            response.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean tieneCookie(HttpServletRequest request, String nombre) {
        if (request.getCookies() == null) {
            return false;
        }
        for (var cookie : request.getCookies()) {
            if (nombre.equals(cookie.getName())) {
                return true;
            }
        }
        return false;
    }

    private boolean noUsaBearer(HttpServletRequest request) {
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        return auth == null || !auth.startsWith("Bearer ");
    }
}
