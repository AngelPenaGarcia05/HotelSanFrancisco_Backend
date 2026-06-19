package com.sanfrancisco.api.modules.seguridad.reniec;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.modules.seguridad.dto.response.ReniecConsultaResponse;
import com.sanfrancisco.api.modules.seguridad.reniec.dto.ReniecApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

/**
 * Cliente HTTP hacia el proveedor RENIEC (apisperu.com).
 * <p>
 * Se aísla en un bean propio para que el cache {@code @Cacheable} opere a través
 * del proxy de Spring incluso cuando {@link ReniecServiceImpl} invoca la consulta
 * desde sus distintos métodos (la auto-invocación dentro del mismo bean omitiría
 * el cache). El token se inyecta como variable de entorno y nunca se loguea.
 */
@Component
public class ReniecClient {

    private static final Logger log = LoggerFactory.getLogger(ReniecClient.class);

    private final RestClient restClient;
    private final String token;
    private final boolean enabled;

    public ReniecClient(
            @Value("${app.reniec.api-url:https://dniruc.apisperu.com/api/v1/dni}") String apiUrl,
            @Value("${app.reniec.token:}") String token,
            @Value("${app.reniec.enabled:true}") boolean enabled) {
        this.token = token;
        this.enabled = enabled;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(8));

        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .requestFactory(factory)
                .build();
    }

    /**
     * Realiza la consulta al proveedor. El resultado se cachea por DNI (Caffeine,
     * cache {@code reniecDni}). Solo se cachean consultas exitosas: cualquier fallo
     * lanza {@link BusinessException} y no se almacena.
     *
     * @param dni DNI ya validado (8 dígitos)
     */
    @Cacheable(value = "reniecDni", key = "#dni")
    public ReniecConsultaResponse fetch(String dni) {
        if (!enabled) {
            throw new BusinessException("La integración con RENIEC está deshabilitada.");
        }
        if (token == null || token.isBlank()) {
            log.error("Consulta RENIEC solicitada pero el token (app.reniec.token) no está configurado.");
            throw new BusinessException("El servicio de consulta RENIEC no está disponible en este momento.");
        }

        try {
            ReniecApiResponse resp = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/{dni}")
                            .queryParam("token", token)
                            .build(dni))
                    .retrieve()
                    .body(ReniecApiResponse.class);

            if (resp == null || resp.nombres() == null || resp.nombres().isBlank()) {
                throw new BusinessException("No se encontraron datos para el DNI " + dni + " en RENIEC.");
            }

            return toResponse(dni, resp);

        } catch (HttpClientErrorException e) {
            int status = e.getStatusCode().value();
            if (status == 404 || status == 422) {
                throw new BusinessException("No se encontraron datos para el DNI " + dni + " en RENIEC.");
            }
            if (status == 401 || status == 403) {
                log.error("Token RENIEC inválido o sin autorización (HTTP {}).", status);
                throw new BusinessException("El servicio de consulta RENIEC no está disponible en este momento.");
            }
            log.warn("Respuesta de error de RENIEC (HTTP {}) para DNI {}.", status, dni);
            throw new BusinessException("No se pudo consultar el DNI en RENIEC.");
        } catch (RestClientException e) {
            log.warn("Fallo de comunicación con RENIEC para DNI {}: {}", dni, e.getMessage());
            throw new BusinessException("El servicio de consulta RENIEC no está disponible en este momento.");
        }
    }

    private ReniecConsultaResponse toResponse(String dni, ReniecApiResponse resp) {
        String nombres = safe(resp.nombres());
        String apPaterno = safe(resp.apellidoPaterno());
        String apMaterno = safe(resp.apellidoMaterno());
        String nombreCompleto = (nombres + " " + apPaterno + " " + apMaterno).trim().replaceAll("\\s+", " ");
        return new ReniecConsultaResponse(dni, nombres, apPaterno, apMaterno, nombreCompleto);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
