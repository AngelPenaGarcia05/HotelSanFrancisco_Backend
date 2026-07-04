package com.sanfrancisco.api.modules.seguridad.security;

import tools.jackson.databind.ObjectMapper;
import com.sanfrancisco.api.shared.api.ErrorResponse;
import com.sanfrancisco.api.shared.utils.ClientIpResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 120;

    // La consulta RENIEC es pública y devuelve datos personales (nombres por DNI):
    // un límite propio y estricto evita que se use para enumerar el padrón.
    private static final int MAX_RENIEC_PER_MINUTE = 10;
    private static final String RENIEC_PATH_PREFIX = "/auth/reniec/";

    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    public RateLimitingFilter(CacheManager cacheManager, ObjectMapper objectMapper) {
        this.cacheManager = cacheManager;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/ws")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = ClientIpResolver.resolve(request);
        boolean esReniec = path.startsWith(RENIEC_PATH_PREFIX);
        int limite = esReniec ? MAX_RENIEC_PER_MINUTE : MAX_REQUESTS_PER_MINUTE;
        String cacheKey = "rate_limit:" + (esReniec ? "reniec:" : "") + clientIp
                + ":" + (Instant.now().getEpochSecond() / 60);

        Cache cache = cacheManager.getCache("rateLimit");
        if (cache != null) {
            Integer count = cache.get(cacheKey, Integer.class);
            if (count == null) {
                count = 0;
            }

            if (count >= limite) {
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

                ErrorResponse errorResponse = new ErrorResponse(
                        false,
                        "RATE_LIMIT_EXCEEDED",
                        "Límite de peticiones excedido - Intente de nuevo más tarde",
                        request.getRequestURI(),
                        null,
                        null,
                        Instant.now()
                );

                response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                return;
            }

            count++;
            cache.put(cacheKey, count);
        }

        filterChain.doFilter(request, response);
    }
}
