package com.sanfrancisco.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración base de la documentación OpenAPI (Swagger UI).
 * <p>
 * La autenticación es por cookie HttpOnly ({@code access_token}); se documenta
 * como un esquema apiKey en cookie. Como Swagger UI corre en el mismo origen que
 * la API, tras iniciar sesión vía {@code POST /auth/login} el navegador adjunta
 * la cookie automáticamente en las siguientes peticiones.
 */
@Configuration
public class OpenApiConfig {

    public static final String COOKIE_AUTH_SCHEME = "cookieAuth";

    @Bean
    public OpenAPI hotelSanFranciscoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hotel San Francisco — API")
                        .description("""
                                API REST del sistema de gestión hotelera Hotel San Francisco.
                                Cubre autenticación, usuarios y roles (RBAC con permisos granulares),
                                recepción, reservas, pagos, ventas, inventario, RRHH, notificaciones,
                                auditoría y dashboard.

                                **Autenticación:** basada en JWT en cookies HttpOnly. Inicia sesión en
                                `POST /auth/login`; la cookie de acceso se envía automáticamente en las
                                siguientes peticiones del mismo origen.""")
                        .version("v1")
                        .contact(new Contact()
                                .name("Equipo Hotel San Francisco")
                                .email("soporte@hotelsanfrancisco.pe"))
                        .license(new License().name("Uso interno / académico")))
                .components(new Components()
                        .addSecuritySchemes(COOKIE_AUTH_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("access_token")
                                .description("JWT de acceso almacenado en cookie HttpOnly, emitido por /auth/login.")));
    }
}
