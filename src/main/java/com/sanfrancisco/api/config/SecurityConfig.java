package com.sanfrancisco.api.config;

import com.sanfrancisco.api.config.security.EndpointPaths;
import com.sanfrancisco.api.config.security.Permissions;
import com.sanfrancisco.api.modules.seguridad.security.CsrfHeaderFilter;
import com.sanfrancisco.api.modules.seguridad.security.HttpAccessDeniedHandler;
import com.sanfrancisco.api.modules.seguridad.security.HttpAuthenticationEntryPoint;
import com.sanfrancisco.api.modules.seguridad.security.JwtAuthenticationFilter;
import com.sanfrancisco.api.modules.seguridad.security.RateLimitingFilter;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final CsrfHeaderFilter csrfHeaderFilter;
    private final HttpAuthenticationEntryPoint authenticationEntryPoint;
    private final HttpAccessDeniedHandler accessDeniedHandler;

    @Value("${app.cors.allowed-origins:http://localhost:4200,http://127.0.0.1:4200}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          RateLimitingFilter rateLimitingFilter,
                          CsrfHeaderFilter csrfHeaderFilter,
                          HttpAuthenticationEntryPoint authenticationEntryPoint,
                          HttpAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitingFilter = rateLimitingFilter;
        this.csrfHeaderFilter = csrfHeaderFilter;
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
                    EndpointPaths.AUTH_LOGOUT,
                    EndpointPaths.AUTH_FORGOT_PASSWORD,
                    EndpointPaths.AUTH_RESET_PASSWORD,
                    EndpointPaths.AUTH_VERIFY_EMAIL,
                    EndpointPaths.AUTH_RESEND_VERIFICATION
                ).permitAll()
                .requestMatchers(HttpMethod.GET, EndpointPaths.AUTH_RENIEC_BASE + "/**").permitAll()
                .requestMatchers(EndpointPaths.WS_BASE + "/**").permitAll()

                // Healthcheck público (Railway / monitoreo)
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()

                // Documentación OpenAPI / Swagger UI
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs",
                    "/v3/api-docs/**"
                ).permitAll()
                
                // =============================================================
                // BOOKING — Flujo público de reservas (sin autenticación)
                // =============================================================
                .requestMatchers(HttpMethod.GET,  EndpointPaths.BOOKING_BASE + "/disponibles").permitAll()
                .requestMatchers(HttpMethod.GET,  EndpointPaths.BOOKING_BASE + "/metodos-pago").permitAll()
                .requestMatchers(HttpMethod.POST, EndpointPaths.BOOKING_BASE).permitAll()
                .requestMatchers(HttpMethod.POST, EndpointPaths.BOOKING_BASE + "/*/pago/session").permitAll()
                .requestMatchers(HttpMethod.POST, EndpointPaths.BOOKING_BASE + "/pago/confirmar").permitAll()
                .requestMatchers(HttpMethod.POST, EndpointPaths.BOOKING_BASE + "/pago/retorno/*").permitAll()
                .requestMatchers(HttpMethod.GET,  EndpointPaths.BOOKING_BASE + "/pago/*/confirmacion").permitAll()

                // =============================================================
                // MIS RESERVAS — Panel del cliente autenticado
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.MIS_RESERVAS_BASE + "/**")
                    .hasAuthority(Permissions.MIS_RESERVAS_READ)
                .requestMatchers(HttpMethod.POST, EndpointPaths.MIS_RESERVAS_BASE)
                    .hasAuthority(Permissions.MIS_RESERVAS_CREATE)
                .requestMatchers(HttpMethod.PATCH, EndpointPaths.MIS_RESERVAS_BASE + "/**")
                    .hasAuthority(Permissions.MIS_RESERVAS_UPDATE)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.MIS_RESERVAS_BASE + "/**")
                    .hasAuthority(Permissions.MIS_RESERVAS_DELETE)

                // =============================================================
                // MIS PAGOS / MIS FACTURAS — Panel del cliente autenticado
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.MIS_PAGOS_BASE + "/**")
                    .hasAuthority(Permissions.MIS_PAGOS_READ)
                .requestMatchers(HttpMethod.GET, EndpointPaths.MIS_FACTURAS_BASE + "/**")
                    .hasAuthority(Permissions.MIS_PAGOS_READ)

                // =============================================================
                // MIS SERVICIOS — Pedidos de servicio del cliente autenticado
                // =============================================================
                .requestMatchers(HttpMethod.POST, EndpointPaths.MIS_SERVICIOS_BASE)
                    .hasAuthority(Permissions.MIS_SERVICIOS_CREATE)
                .requestMatchers(HttpMethod.PATCH, EndpointPaths.MIS_SERVICIOS_BASE + "/**")
                    .hasAuthority(Permissions.MIS_SERVICIOS_CREATE)
                .requestMatchers(HttpMethod.GET, EndpointPaths.MIS_SERVICIOS_BASE + "/**")
                    .hasAuthority(Permissions.MIS_SERVICIOS_READ)

                // =============================================================
                // TIPO HABITACIÓN — catálogo visible en la landing page sin sesión
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.TIPO_HABITACION_BASE + "/**").permitAll()
                
                // =============================================================
                // PÚBLICO — Reserva online y consulta de disponibilidad
                // =============================================================
                .requestMatchers(EndpointPaths.PUBLIC_BASE + "/**").permitAll()
                .requestMatchers(HttpMethod.GET, EndpointPaths.RESERVA_BASE + "/disponibilidad").permitAll()

                // Auth endpoints que requieren sesión activa
                .requestMatchers(
                    EndpointPaths.AUTH_ME,
                    EndpointPaths.AUTH_LOGOUT_ALL,
                    EndpointPaths.AUTH_CHANGE_PASSWORD
                ).authenticated()

                // =============================================================
                // CLIENTES
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.CLIENTE_BASE + "/**")
                    .hasAuthority(Permissions.CLIENTE_READ)
                .requestMatchers(HttpMethod.POST, EndpointPaths.CLIENTE_BASE)
                    .hasAuthority(Permissions.CLIENTE_CREATE)
                .requestMatchers(HttpMethod.PUT, EndpointPaths.CLIENTE_BASE + "/**")
                    .hasAuthority(Permissions.CLIENTE_UPDATE)
                .requestMatchers(HttpMethod.PATCH, EndpointPaths.CLIENTE_BASE + "/**")
                    .hasAuthority(Permissions.CLIENTE_CHANGE_STATUS)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.CLIENTE_BASE + "/**")
                    .hasAuthority(Permissions.CLIENTE_DELETE)

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
                // NOTIFICACIONES DEL CLIENTE — Bandeja in-app (rol CLIENTE).
                // Rutas específicas declaradas ANTES del comodín admin.
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.NOTIFICACIONES_BASE)
                    .hasAuthority(Permissions.NOTIFICACION_CLIENTE_READ)
                .requestMatchers(HttpMethod.PATCH, EndpointPaths.NOTIFICACIONES_BASE + "/leer-todas")
                    .hasAuthority(Permissions.NOTIFICACION_CLIENTE_UPDATE)

                // =============================================================
                // NOTIFICACIONES — Configuración SMTP, plantillas, log de envíos.
                // Requiere usuario:read (ADMIN, RRHH). Clientes excluidos.
                // =============================================================
                .requestMatchers(EndpointPaths.NOTIFICACIONES_BASE + "/**")
                    .hasAuthority(Permissions.USUARIO_READ)

                // =============================================================
                // REPORTES — Ingresos, reservas, ocupación, dashboard gerencial.
                // Permiso dedicado reporte:read (ADMIN, CAJA). Desacoplado de
                // pago:read para que RECEPCION no acceda a reportes financieros.
                // El de nómina es información salarial: exige nomina:read
                // (ADMIN/RRHH) y debe declararse ANTES de la regla general.
                // =============================================================
                .requestMatchers(EndpointPaths.REPORTES_BASE + "/nomina",
                                 EndpointPaths.REPORTES_BASE + "/nomina/**")
                    .hasAuthority(Permissions.NOMINA_READ)
                .requestMatchers(EndpointPaths.REPORTES_BASE + "/asistencia",
                                 EndpointPaths.REPORTES_BASE + "/asistencia/**")
                    .hasAuthority(Permissions.ASISTENCIA_READ)
                .requestMatchers(EndpointPaths.REPORTES_BASE + "/**")
                    .hasAuthority(Permissions.REPORTE_READ)

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
                // PEDIDOS DE SERVICIO — Gestión por recepción/admin.
                // Reutiliza permisos de servicios: lectura servicio:read,
                // aprobar/rechazar servicio:create (aprobar genera el consumo).
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.PEDIDO_SERVICIO_BASE + "/**")
                    .hasAuthority(Permissions.SERVICIO_READ)
                .requestMatchers(HttpMethod.PATCH, EndpointPaths.PEDIDO_SERVICIO_BASE + "/**")
                    .hasAuthority(Permissions.SERVICIO_CREATE)

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
                // CATÁLOGO DE SERVICIOS — Lectura para el cliente autenticado.
                // Devuelve solo los servicios ACTIVO. CLIENTE no tiene
                // tipo-servicio:read, por eso se expone un permiso propio.
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.SERVICIO_CATALOGO_BASE + "/**")
                    .hasAuthority(Permissions.SERVICIO_CATALOGO_READ)

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
                // TURNOS — planificación de personal (ADMIN / RRHH)
                // =============================================================
                .requestMatchers(HttpMethod.POST, EndpointPaths.TURNO_BASE + "/generar")
                    .hasAuthority(Permissions.TURNOS_GENERAR)
                .requestMatchers(HttpMethod.GET, EndpointPaths.TURNO_BASE + "/**")
                    .hasAuthority(Permissions.TURNOS_READ)
                .requestMatchers(HttpMethod.PATCH, EndpointPaths.TURNO_BASE + "/**")
                    .hasAuthority(Permissions.TURNOS_UPDATE)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.TURNO_BASE + "/**")
                    .hasAuthority(Permissions.TURNOS_DELETE)

                // =============================================================
                // MI ASISTENCIA — marcado self-service del empleado autenticado
                // =============================================================
                .requestMatchers(HttpMethod.POST, EndpointPaths.MI_ASISTENCIA_BASE + "/**")
                    .hasAuthority(Permissions.MI_ASISTENCIA_MARCAR)
                .requestMatchers(HttpMethod.GET, EndpointPaths.MI_ASISTENCIA_BASE + "/**")
                    .hasAuthority(Permissions.MI_ASISTENCIA_READ)

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
                .requestMatchers(HttpMethod.POST, EndpointPaths.PAGO_NOMINA_BASE + "/calcular")
                    .hasAuthority(Permissions.NOMINA_CREATE)
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
                // DASHBOARD — Cualquier usuario autenticado; el contenido se
                // filtra internamente según los permisos granulares del usuario.
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.DASHBOARD_BASE + "/**")
                    .authenticated()

                // =============================================================
                // AUDITORÍA — Solo lectura (ADMIN vía permiso auditoria:read)
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.AUDITORIA_BASE + "/**")
                    .hasAuthority(Permissions.AUDITORIA_READ)

                // =============================================================
                // SOLICITUDES DE SERVICIO
                // El filtrado "propias vs todas" se resuelve en la capa de servicio.
                // Rutas específicas declaradas antes que los comodines.
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.SOLICITUD_BASE + "/reporte")
                    .hasAuthority(Permissions.SOLICITUD_REPORT)
                .requestMatchers(HttpMethod.POST, EndpointPaths.SOLICITUD_BASE + "/*/seguimientos")
                    .hasAuthority(Permissions.SOLICITUD_CHANGE_STATUS)
                .requestMatchers(HttpMethod.PATCH, EndpointPaths.SOLICITUD_BASE + "/*/asignar")
                    .hasAuthority(Permissions.SOLICITUD_ASSIGN)
                .requestMatchers(HttpMethod.PATCH, EndpointPaths.SOLICITUD_BASE + "/*/estado")
                    .hasAuthority(Permissions.SOLICITUD_CHANGE_STATUS)
                .requestMatchers(HttpMethod.POST, EndpointPaths.SOLICITUD_BASE)
                    .hasAuthority(Permissions.SOLICITUD_CREATE)
                .requestMatchers(HttpMethod.GET, EndpointPaths.SOLICITUD_BASE + "/**")
                    .hasAuthority(Permissions.SOLICITUD_READ)
                .requestMatchers(HttpMethod.PUT, EndpointPaths.SOLICITUD_BASE + "/**")
                    .hasAuthority(Permissions.SOLICITUD_UPDATE)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.SOLICITUD_BASE + "/**")
                    .hasAuthority(Permissions.SOLICITUD_DELETE)

                // =============================================================
                // SEGURIDAD — Usuarios, Roles, Permisos, Tipos de Documento
                // =============================================================
                .requestMatchers(HttpMethod.GET, EndpointPaths.USUARIO_BASE + "/**")
                    .hasAuthority(Permissions.USUARIO_READ)
                .requestMatchers(HttpMethod.POST, EndpointPaths.USUARIO_BASE)
                    .hasAuthority(Permissions.USUARIO_CREATE)
                .requestMatchers(HttpMethod.PUT, EndpointPaths.USUARIO_BASE + "/**")
                    .hasAuthority(Permissions.USUARIO_UPDATE)
                // Asignación de rol — requiere usuario:update (debe ir antes del PATCH genérico)
                .requestMatchers(HttpMethod.PATCH, EndpointPaths.USUARIO_BASE + "/*/rol")
                    .hasAuthority(Permissions.USUARIO_UPDATE)
                .requestMatchers(HttpMethod.PATCH, EndpointPaths.USUARIO_BASE + "/**")
                    .hasAuthority(Permissions.USUARIO_CHANGE_STATUS)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.USUARIO_BASE + "/**")
                    .hasAuthority(Permissions.USUARIO_DELETE)

                .requestMatchers(HttpMethod.GET, EndpointPaths.ROL_BASE + "/**")
                    .hasAuthority(Permissions.ROL_READ)
                // Gestión granular de permisos del rol — requiere rol:update
                // (debe ir antes de las reglas POST/DELETE genéricas de roles)
                .requestMatchers(HttpMethod.POST, EndpointPaths.ROL_BASE + "/*/permisos")
                    .hasAuthority(Permissions.ROL_UPDATE)
                .requestMatchers(HttpMethod.DELETE, EndpointPaths.ROL_BASE + "/*/permisos/**")
                    .hasAuthority(Permissions.ROL_UPDATE)
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
            .addFilterBefore(csrfHeaderFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(o -> !o.isEmpty())
                .toList();
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-XSRF-TOKEN", "Cache-Control", "X-Requested-With"));
        // Content-Disposition debe exponerse para que el frontend pueda leer el
        // nombre del archivo en las descargas (exportación de reportes).
        config.setExposedHeaders(List.of("Set-Cookie", "Content-Disposition"));
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