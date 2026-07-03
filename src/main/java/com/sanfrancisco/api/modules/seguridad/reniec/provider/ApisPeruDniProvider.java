package com.sanfrancisco.api.modules.seguridad.reniec.provider;

import com.sanfrancisco.api.modules.seguridad.dto.response.ReniecConsultaResponse;
import com.sanfrancisco.api.modules.seguridad.reniec.dto.ReniecApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.Optional;

/**
 * Proveedor primario: apisperu.com ({@code GET /api/v1/dni/{dni}?token=...}).
 * El token se inyecta como variable de entorno y nunca se loguea.
 */
@Component
@Order(1)
public class ApisPeruDniProvider implements DniProvider {

    private static final Logger log = LoggerFactory.getLogger(ApisPeruDniProvider.class);

    private final RestClient restClient;
    private final String token;
    private final boolean enabled;

    public ApisPeruDniProvider(
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

    @Override
    public String nombre() {
        return "apisperu";
    }

    @Override
    public boolean disponible() {
        if (!enabled) {
            return false;
        }
        if (token == null || token.isBlank()) {
            log.warn("Proveedor apisperu habilitado pero sin token (app.reniec.token); se omite.");
            return false;
        }
        return true;
    }

    @Override
    public Optional<ReniecConsultaResponse> consultar(String dni) {
        try {
            ReniecApiResponse resp = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/{dni}")
                            .queryParam("token", token)
                            .build(dni))
                    .retrieve()
                    .body(ReniecApiResponse.class);

            if (resp == null || resp.nombres() == null || resp.nombres().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(toResponse(dni, resp));

        } catch (HttpClientErrorException e) {
            int status = e.getStatusCode().value();
            if (status == 404 || status == 422) {
                return Optional.empty();
            }
            if (status == 401 || status == 403) {
                throw new DniProviderException("token inválido o sin autorización (HTTP " + status + ")", e);
            }
            throw new DniProviderException("respuesta de error HTTP " + status, e);
        } catch (RestClientException e) {
            throw new DniProviderException("fallo de comunicación: " + e.getMessage(), e);
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
