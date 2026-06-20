package com.sanfrancisco.api.modules.seguridad.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/auth/login") || path.startsWith("/auth/refresh") || path.startsWith("/ws")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = jwtService.extractTokenFromCookie(request, JwtService.ACCESS_TOKEN_COOKIE);

        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        if (token != null && jwtService.validateToken(token)) {
            Claims claims = jwtService.extractClaims(token);
            String email = claims.getSubject();

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Integer userId = safeInteger(claims.get("userId"));

                Collection<GrantedAuthority> authorities = resolveAuthorities(claims, email);

                UserPrincipal principal = new UserPrincipal(userId, email, authorities);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Construye las authorities priorizando los permisos del JWT.
     * <p>
     * SecurityConfig autoriza por permiso (p. ej. {@code "reserva:read"}), no por rol,
     * así que las authorities efectivas son los códigos de permiso. El rol se incluye
     * adicionalmente como {@code ROLE_xxx} solo para soportar reglas basadas en rol.
     * <p>
     * Si el claim {@code permissions} viene ausente o vacío (token emitido por una
     * versión previa, o serialización defectuosa), se recargan los permisos desde la BD
     * vía {@link CustomUserDetailsService}. Esto garantiza que un rol con permisos en BD
     * siempre obtenga sus authorities efectivas y evita 403 espurios.
     */
    private Collection<GrantedAuthority> resolveAuthorities(Claims claims, String email) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        Object rawPermissions = claims.get("permissions");
        if (rawPermissions instanceof List<?> list) {
            for (Object p : list) {
                if (p instanceof String s && !s.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority(s));
                }
            }
        }

        if (authorities.isEmpty()) {
            log.debug("JWT sin claim 'permissions' utilizable para {}. Recargando authorities desde BD.", email);
            try {
                UserDetails fresh = userDetailsService.loadUserByUsername(email);
                for (GrantedAuthority ga : fresh.getAuthorities()) {
                    String name = ga.getAuthority();
                    if (name != null && !name.startsWith("ROLE_")) {
                        authorities.add(new SimpleGrantedAuthority(name));
                    }
                }
            } catch (Exception ex) {
                log.warn("No se pudieron recargar authorities desde BD para {}: {}", email, ex.getMessage());
            }
        }

        Object rawRole = claims.get("role");
        if (rawRole instanceof String role && !role.isBlank()) {
            String roleName = role.toUpperCase();
            if (!roleName.startsWith("ROLE_")) {
                roleName = "ROLE_" + roleName;
            }
            authorities.add(new SimpleGrantedAuthority(roleName));
        }

        return authorities;
    }

    private Integer safeInteger(Object value) {
        if (value instanceof Integer i) return i;
        if (value instanceof Number n) return n.intValue();
        return null;
    }
}
