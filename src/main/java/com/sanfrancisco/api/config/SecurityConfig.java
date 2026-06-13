package com.sanfrancisco.api.config;

import com.sanfrancisco.api.config.security.EndpointPaths;
import com.sanfrancisco.api.config.security.Permissions;
import com.sanfrancisco.api.modules.seguridad.security.HttpAccessDeniedHandler;
import com.sanfrancisco.api.modules.seguridad.security.HttpAuthenticationEntryPoint;
import com.sanfrancisco.api.modules.seguridad.security.JwtAuthenticationFilter;
import com.sanfrancisco.api.modules.seguridad.security.RateLimitingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final HttpAuthenticationEntryPoint authenticationEntryPoint;
    private final HttpAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          RateLimitingFilter rateLimitingFilter,
                          HttpAuthenticationEntryPoint authenticationEntryPoint,
                          HttpAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitingFilter = rateLimitingFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // CSRF deshabilitado: la API es stateless con JWT en cookies HttpOnly + SameSite=Lax.
            // El JWT en cookie HttpOnly no es accesible por JavaScript de terceros y SameSite=Lax
            // bloquea los POST cross-site, lo que provee la protección equivalente a CSRF.
            // Mantener double-submit (XSRF-TOKEN) obligaría al SPA a leer la cookie y reenviarla
            // como header en cada request, lo cual es redundante cuando ya hay JWT autenticando.
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; frame-ancestors 'none'; object-src 'none';"))
            )
            .authorizeHttpRequests(auth -> auth

                // =============================================================
                // AUTH — Endpoints públicos
                // =============================================================
                .requestMatchers(
                    EndpointPaths.AUTH_LOGIN,
                    EndpointPaths.AUTH_REGISTER,
                    EndpointPaths.AUTH_DOCUMENT_TYPES,
                    EndpointPaths.AUTH_REFRESH,
                    EndpointPaths.AUTH_LOGOUT
                ).permitAll()
                .requestMatchers(EndpointPaths.WS_BASE + "/**").permitAll()

                // Auth endpoints que requieren sesión activa
                .requestMatchers(
                    EndpointPaths.AUTH_ME,
                    EndpointPaths.AUTH_LOGOUT_ALL,
                    EndpointPaths.AUTH_CHANGE_PASSWORD
                ).authenticated()

                // =============================================================
                // RESERVAS
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.RESERVA_BASE + "/**")
                    .hasAuthority(Permissions.RESERVA_READ)
                .requestMatchers(HttpMethod.POST, EndpointPaths.RESERVA_BASE)
                    .hasAuthority(Permissions.RESERVA_CREATE)
                .requestMatchers(HttpMethod.PUT, EndpointPaths.RESERVA_BASE + "/**")
                    .hasAuthority(Permissions.RESERVA_UPDATE)
                .requestMatchers(HttpMethod.PATCH, EndpointPaths.RESERVA_BASE + "/**")
                    .hasAuthority(Permissions.RESERVA_CHANGE_STATUS)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.RESERVA_BASE + "/**")
                    .hasAuthority(Permissions.RESERVA_DELETE)

                // =============================================================
                // HABITACIONES (unidades físicas)
                // =============================================================
                .requestMatchers(HttpMethod.GET,   EndpointPaths.HABITACION_BASE + "/**")
                    .hasAuthority(Permissions.HABITACION_READ)
                .requestMatchers(HttpMethod.POST,  EndpointPaths.HABITACION_BASE + "/checkin")
                    .hasAuthority(Permissions.HABITACION_CHECKIN)
                .requestMatchers(HttpMethod.POST,  EndpointPaths.HABITACION_BASE + "/checkout")
                    .hasAuthority(Permissions.HABITACION_CHECKOUT)
                .requestMatchers(HttpMethod.PATCH, EndpointPaths.HABITACION_BASE + "/*/limpieza-completada")
                    .hasAuthority(Permissions.HABITACION_LIMPIEZA)
                .requestMatchers(HttpMethod.POST,  EndpointPaths.HABITACION_BASE)
                    .hasAuthority(Permissions.HABITACION_CREATE)
                .requestMatchers(HttpMethod.PUT,   EndpointPaths.HABITACION_BASE + "/**")
                    .hasAuthority(Permissions.HABITACION_UPDATE)
                .requestMatchers(HttpMethod.PATCH, EndpointPaths.HABITACION_BASE + "/**")
                    .hasAuthority(Permissions.HABITACION_CHANGE_STATUS)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.HABITACION_BASE + "/**")
                    .hasAuthority(Permissions.HABITACION_DELETE)

                // =============================================================
                // TIPO HABITACIÓN
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.TIPO_HABITACION_BASE + "/**")
                    .hasAuthority(Permissions.TIPO_HABITACION_READ)
                .requestMatchers(HttpMethod.POST, EndpointPaths.TIPO_HABITACION_BASE)
                    .hasAuthority(Permissions.TIPO_HABITACION_CREATE)
                .requestMatchers(HttpMethod.PUT, EndpointPaths.TIPO_HABITACION_BASE + "/**")
                    .hasAuthority(Permissions.TIPO_HABITACION_UPDATE)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.TIPO_HABITACION_BASE + "/**")
                    .hasAuthority(Permissions.TIPO_HABITACION_DELETE)

                // =============================================================
                // PAGOS
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.PAGO_BASE + "/**")
                    .hasAuthority(Permissions.PAGO_READ)
                .requestMatchers(HttpMethod.POST, EndpointPaths.PAGO_BASE)
                    .hasAuthority(Permissions.PAGO_CREATE)
                .requestMatchers(HttpMethod.PUT, EndpointPaths.PAGO_BASE + "/**")
                    .hasAuthority(Permissions.PAGO_UPDATE)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.PAGO_BASE + "/**")
                    .hasAuthority(Permissions.PAGO_DELETE)

                // =============================================================
                // MÉTODOS DE PAGO
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.METODO_PAGO_BASE + "/**")
                    .hasAuthority(Permissions.METODO_PAGO_READ)
                .requestMatchers(HttpMethod.POST, EndpointPaths.METODO_PAGO_BASE)
                    .hasAuthority(Permissions.METODO_PAGO_CREATE)
                .requestMatchers(HttpMethod.PUT, EndpointPaths.METODO_PAGO_BASE + "/**")
                    .hasAuthority(Permissions.METODO_PAGO_UPDATE)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.METODO_PAGO_BASE + "/**")
                    .hasAuthority(Permissions.METODO_PAGO_DELETE)

                // =============================================================
                // VENTAS
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.VENTA_BASE + "/**")
                    .hasAuthority(Permissions.VENTA_READ)
                .requestMatchers(HttpMethod.POST, EndpointPaths.VENTA_BASE)
                    .hasAuthority(Permissions.VENTA_CREATE)
                .requestMatchers(HttpMethod.PUT, EndpointPaths.VENTA_BASE + "/**")
                    .hasAuthority(Permissions.VENTA_UPDATE)
                .requestMatchers(HttpMethod.PATCH, EndpointPaths.VENTA_BASE + "/**")
                    .hasAuthority(Permissions.VENTA_CHANGE_STATUS)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.VENTA_BASE + "/**")
                    .hasAuthority(Permissions.VENTA_DELETE)

                // =============================================================
                // PRODUCTOS
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.PRODUCTO_BASE + "/**")
                    .hasAuthority(Permissions.PRODUCTO_READ)
                .requestMatchers(HttpMethod.POST, EndpointPaths.PRODUCTO_BASE)
                    .hasAuthority(Permissions.PRODUCTO_CREATE)
                .requestMatchers(HttpMethod.PUT, EndpointPaths.PRODUCTO_BASE + "/**")
                    .hasAuthority(Permissions.PRODUCTO_UPDATE)
                .requestMatchers(HttpMethod.PATCH, EndpointPaths.PRODUCTO_BASE + "/**")
                    .hasAuthority(Permissions.PRODUCTO_ADJUST_STOCK)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.PRODUCTO_BASE + "/**")
                    .hasAuthority(Permissions.PRODUCTO_DELETE)

                // =============================================================
                // CATEGORÍAS DE PRODUCTO
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.CATEGORIA_PRODUCTO_BASE + "/**")
                    .hasAuthority(Permissions.CATEGORIA_PRODUCTO_READ)
                .requestMatchers(HttpMethod.POST, EndpointPaths.CATEGORIA_PRODUCTO_BASE)
                    .hasAuthority(Permissions.CATEGORIA_PRODUCTO_CREATE)
                .requestMatchers(HttpMethod.PUT, EndpointPaths.CATEGORIA_PRODUCTO_BASE + "/**")
                    .hasAuthority(Permissions.CATEGORIA_PRODUCTO_UPDATE)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.CATEGORIA_PRODUCTO_BASE + "/**")
                    .hasAuthority(Permissions.CATEGORIA_PRODUCTO_DELETE)

                // =============================================================
                // COMPRAS
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.COMPRA_BASE + "/**")
                    .hasAuthority(Permissions.COMPRA_READ)
                .requestMatchers(HttpMethod.POST, EndpointPaths.COMPRA_BASE)
                    .hasAuthority(Permissions.COMPRA_CREATE)
                .requestMatchers(HttpMethod.PUT, EndpointPaths.COMPRA_BASE + "/**")
                    .hasAuthority(Permissions.COMPRA_UPDATE)
                .requestMatchers(HttpMethod.PATCH, EndpointPaths.COMPRA_BASE + "/**")
                    .hasAuthority(Permissions.COMPRA_CHANGE_STATUS)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.COMPRA_BASE + "/**")
                    .hasAuthority(Permissions.COMPRA_DELETE)

                // =============================================================
                // PROVEEDORES
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.PROVEEDOR_BASE + "/**")
                    .hasAuthority(Permissions.PROVEEDOR_READ)
                .requestMatchers(HttpMethod.POST, EndpointPaths.PROVEEDOR_BASE)
                    .hasAuthority(Permissions.PROVEEDOR_CREATE)
                .requestMatchers(HttpMethod.PUT, EndpointPaths.PROVEEDOR_BASE + "/**")
                    .hasAuthority(Permissions.PROVEEDOR_UPDATE)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.PROVEEDOR_BASE + "/**")
                    .hasAuthority(Permissions.PROVEEDOR_DELETE)

                // =============================================================
                // SERVICIOS
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.SERVICIO_BASE + "/**")
                    .hasAuthority(Permissions.SERVICIO_READ)
                .requestMatchers(HttpMethod.POST, EndpointPaths.SERVICIO_BASE)
                    .hasAuthority(Permissions.SERVICIO_CREATE)
                .requestMatchers(HttpMethod.PUT, EndpointPaths.SERVICIO_BASE + "/**")
                    .hasAuthority(Permissions.SERVICIO_UPDATE)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.SERVICIO_BASE + "/**")
                    .hasAuthority(Permissions.SERVICIO_DELETE)

                // =============================================================
                // TIPOS DE SERVICIO
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.TIPO_SERVICIO_BASE + "/**")
                    .hasAuthority(Permissions.TIPO_SERVICIO_READ)
                .requestMatchers(HttpMethod.POST, EndpointPaths.TIPO_SERVICIO_BASE)
                    .hasAuthority(Permissions.TIPO_SERVICIO_CREATE)
                .requestMatchers(HttpMethod.PUT, EndpointPaths.TIPO_SERVICIO_BASE + "/**")
                    .hasAuthority(Permissions.TIPO_SERVICIO_UPDATE)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.TIPO_SERVICIO_BASE + "/**")
                    .hasAuthority(Permissions.TIPO_SERVICIO_DELETE)

                // =============================================================
                // INCIDENCIAS
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.INCIDENCIA_BASE + "/**")
                    .hasAuthority(Permissions.INCIDENCIA_READ)
                .requestMatchers(HttpMethod.POST, EndpointPaths.INCIDENCIA_BASE)
                    .hasAuthority(Permissions.INCIDENCIA_CREATE)
                .requestMatchers(HttpMethod.PUT, EndpointPaths.INCIDENCIA_BASE + "/**")
                    .hasAuthority(Permissions.INCIDENCIA_UPDATE)
                .requestMatchers(HttpMethod.PATCH, EndpointPaths.INCIDENCIA_BASE + "/**")
                    .hasAuthority(Permissions.INCIDENCIA_CHANGE_STATUS)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.INCIDENCIA_BASE + "/**")
                    .hasAuthority(Permissions.INCIDENCIA_DELETE)

                // =============================================================
                // HORARIOS
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.HORARIO_BASE + "/**")
                    .hasAuthority(Permissions.HORARIO_READ)
                .requestMatchers(HttpMethod.POST, EndpointPaths.HORARIO_BASE)
                    .hasAuthority(Permissions.HORARIO_CREATE)
                .requestMatchers(HttpMethod.PUT, EndpointPaths.HORARIO_BASE + "/**")
                    .hasAuthority(Permissions.HORARIO_UPDATE)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.HORARIO_BASE + "/**")
                    .hasAuthority(Permissions.HORARIO_DELETE)

                // =============================================================
                // ASISTENCIA
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.ASISTENCIA_BASE + "/**")
                    .hasAuthority(Permissions.ASISTENCIA_READ)
                .requestMatchers(HttpMethod.POST, EndpointPaths.ASISTENCIA_BASE)
                    .hasAuthority(Permissions.ASISTENCIA_CREATE)
                .requestMatchers(HttpMethod.PUT, EndpointPaths.ASISTENCIA_BASE + "/**")
                    .hasAuthority(Permissions.ASISTENCIA_UPDATE)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.ASISTENCIA_BASE + "/**")
                    .hasAuthority(Permissions.ASISTENCIA_DELETE)

                // =============================================================
                // NÓMINA (PAGOS DE NÓMINA)
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.PAGO_NOMINA_BASE + "/**")
                    .hasAuthority(Permissions.NOMINA_READ)
                .requestMatchers(HttpMethod.POST, EndpointPaths.PAGO_NOMINA_BASE)
                    .hasAuthority(Permissions.NOMINA_CREATE)
                .requestMatchers(HttpMethod.PATCH, EndpointPaths.PAGO_NOMINA_BASE + "/**")
                    .hasAuthority(Permissions.NOMINA_CHANGE_STATUS)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.PAGO_NOMINA_BASE + "/**")
                    .hasAuthority(Permissions.NOMINA_DELETE)

                // =============================================================
                // BONOS
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.BONO_BASE + "/**")
                    .hasAuthority(Permissions.BONO_READ)
                .requestMatchers(HttpMethod.POST, EndpointPaths.BONO_BASE)
                    .hasAuthority(Permissions.BONO_CREATE)
                .requestMatchers(HttpMethod.PUT, EndpointPaths.BONO_BASE + "/**")
                    .hasAuthority(Permissions.BONO_UPDATE)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.BONO_BASE + "/**")
                    .hasAuthority(Permissions.BONO_DELETE)

                // =============================================================
                // ASIGNACIÓN DE HORARIOS
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.ASIGNACION_HORARIO_BASE + "/**")
                    .hasAuthority(Permissions.ASIGNACION_HORARIO_READ)
                .requestMatchers(HttpMethod.POST, EndpointPaths.ASIGNACION_HORARIO_BASE)
                    .hasAuthority(Permissions.ASIGNACION_HORARIO_CREATE)
                .requestMatchers(HttpMethod.PUT, EndpointPaths.ASIGNACION_HORARIO_BASE + "/**")
                    .hasAuthority(Permissions.ASIGNACION_HORARIO_UPDATE)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.ASIGNACION_HORARIO_BASE + "/**")
                    .hasAuthority(Permissions.ASIGNACION_HORARIO_DELETE)

                // =============================================================
                // SEGURIDAD — Usuarios, Roles, Permisos, Tipos de Documento
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.USUARIO_BASE + "/**")
                    .hasAuthority(Permissions.USUARIO_READ)
                .requestMatchers(HttpMethod.POST, EndpointPaths.USUARIO_BASE)
                    .hasAuthority(Permissions.USUARIO_CREATE)
                .requestMatchers(HttpMethod.PUT, EndpointPaths.USUARIO_BASE + "/**")
                    .hasAuthority(Permissions.USUARIO_UPDATE)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.USUARIO_BASE + "/**")
                    .hasAuthority(Permissions.USUARIO_DELETE)

                .requestMatchers(HttpMethod.GET, EndpointPaths.ROL_BASE + "/**")
                    .hasAuthority(Permissions.ROL_READ)
                .requestMatchers(HttpMethod.POST, EndpointPaths.ROL_BASE)
                    .hasAuthority(Permissions.ROL_CREATE)
                .requestMatchers(HttpMethod.PUT, EndpointPaths.ROL_BASE + "/**")
                    .hasAuthority(Permissions.ROL_UPDATE)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.ROL_BASE + "/**")
                    .hasAuthority(Permissions.ROL_DELETE)

                .requestMatchers(HttpMethod.GET, EndpointPaths.PERMISO_BASE + "/**")
                    .hasAuthority(Permissions.PERMISO_READ)

                .requestMatchers(HttpMethod.GET, EndpointPaths.TIPO_DOCUMENTO_BASE + "/**")
                    .hasAuthority(Permissions.TIPO_DOCUMENTO_READ)
                .requestMatchers(HttpMethod.POST, EndpointPaths.TIPO_DOCUMENTO_BASE)
                    .hasAuthority(Permissions.TIPO_DOCUMENTO_CREATE)
                .requestMatchers(HttpMethod.PUT, EndpointPaths.TIPO_DOCUMENTO_BASE + "/**")
                    .hasAuthority(Permissions.TIPO_DOCUMENTO_UPDATE)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.TIPO_DOCUMENTO_BASE + "/**")
                    .hasAuthority(Permissions.TIPO_DOCUMENTO_DELETE)

                // =============================================================
                // CATCH-ALL — Todo lo demás requiere autenticación
                // =============================================================
                .anyRequest().authenticated()
            )
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200", "http://127.0.0.1:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-XSRF-TOKEN", "Cache-Control"));
        config.setExposedHeaders(List.of("Set-Cookie"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
