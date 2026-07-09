package com.sanfrancisco.api.modules.notificaciones.mail;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;

/**
 * Cliente HTTP para el envío de correos mediante la API REST transaccional de
 * Brevo ({@code POST /v3/smtp/email}), como alternativa al SMTP.
 * <p>
 * Motivo: algunos entornos (p. ej. el plan gratuito de Railway) bloquean los
 * puertos SMTP de salida; la API va por HTTPS (443), que no se bloquea.
 * <p>
 * Sigue el mismo patrón que los proveedores RENIEC del proyecto
 * ({@code RestClient} con timeouts y mapeo de errores HTTP). No requiere
 * dependencias nuevas: {@code spring-boot-restclient} ya está en el pom.
 * <p>
 * Este cliente solo transporta el correo; las plantillas, el interpolado, el
 * log y los reintentos siguen viviendo en el servicio de notificaciones.
 */
@Component
public class BrevoMailClient {

    private static final Logger log = LoggerFactory.getLogger(BrevoMailClient.class);

    private final RestClient restClient;
    private final String apiKey;

    public BrevoMailClient(
            @Value("${app.notificaciones.brevo.api-url:https://api.brevo.com/v3}") String apiUrl,
            @Value("${BREVO_API_KEY:}") String apiKey) {
        this.apiKey = apiKey;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .requestFactory(factory)
                .build();
    }

    /**
     * Envía un correo HTML a través de la API de Brevo.
     *
     * @param fromEmail   remitente (debe estar verificado en Brevo)
     * @param fromName    nombre visible del remitente
     * @param to          destinatario
     * @param subject     asunto
     * @param htmlContent cuerpo HTML ya renderizado
     * @param replyTo     dirección de respuesta (opcional; {@code null}/vacío la omite)
     * @throws IllegalStateException si {@code BREVO_API_KEY} no está configurada
     * @throws RuntimeException      si Brevo rechaza el envío o falla la comunicación
     */
    public void enviar(String fromEmail, String fromName, String to, String subject,
                       String htmlContent, String replyTo) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "BREVO_API_KEY no está configurada; no se puede enviar por la API de Brevo.");
        }

        BrevoEmailRequest body = new BrevoEmailRequest(
                new Contact(fromName, fromEmail),
                List.of(new Contact(null, to)),
                subject,
                htmlContent,
                (replyTo == null || replyTo.isBlank()) ? null : new Contact(null, replyTo)
        );

        try {
            restClient.post()
                    .uri("/smtp/email")
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            int status = e.getStatusCode().value();
            String detalle = e.getResponseBodyAsString();
            log.warn("Brevo API rechazó el envío a {} (HTTP {}): {}", to, status, detalle);
            if (status == 401) {
                throw new RuntimeException("Brevo API: clave inválida o no autorizada (HTTP 401).", e);
            }
            if (status == 400) {
                throw new RuntimeException(
                        "Brevo API: solicitud inválida (HTTP 400), posible remitente no verificado. " + detalle, e);
            }
            if (status == 429) {
                throw new RuntimeException("Brevo API: límite de envío excedido (HTTP 429).", e);
            }
            throw new RuntimeException("Brevo API: error HTTP " + status + ". " + detalle, e);
        } catch (RestClientException e) {
            throw new RuntimeException("Brevo API: fallo de comunicación: " + e.getMessage(), e);
        }
    }

    // Estructura del cuerpo JSON de POST /v3/smtp/email (campos nulos omitidos).
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record BrevoEmailRequest(
            Contact sender,
            List<Contact> to,
            String subject,
            String htmlContent,
            Contact replyTo
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record Contact(String name, String email) {}
}
