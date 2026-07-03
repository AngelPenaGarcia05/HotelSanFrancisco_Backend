package com.sanfrancisco.api.modules.seguridad.reniec.provider;

import com.sanfrancisco.api.modules.seguridad.dto.response.ReniecConsultaResponse;
import com.sanfrancisco.api.modules.seguridad.reniec.dto.ApisNetPeApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.Optional;

/**
 * Proveedor de respaldo: apis.net.pe (v2),
 * {@code GET /v2/reniec/dni?numero=...} con token Bearer.
 * <p>
 * Mientras {@code app.reniec.fallback.token} esté vacío, {@link #disponible()}
 * devuelve {@code false} y el sistema se comporta como si solo existiera el
 * proveedor primario; al configurar la variable de entorno el fallback se
 * activa sin recompilar.
 */
@Component
@Order(2)
public class ApisNetPeDniProvider implements DniProvider {

    private static final Logger log = LoggerFactory.getLogger(ApisNetPeDniProvider.class);

    private final RestClient restClient;
    private final String token;
    private final boolean enabled;

    public ApisNetPeDniProvider(
            @Value("${app.reniec.fallback.api-url:https://api.apis.net.pe/v2/reniec/dni}") String apiUrl,
            @Value("${app.reniec.fallback.token:}") String token,
            @Value("${app.reniec.fallback.enabled:true}") boolean enabled) {
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
        return "apis.net.pe";
    }

    @Override
    public boolean disponible() {
        if (!enabled) {
            return false;
        }
        if (token == null || token.isBlank()) {
            log.debug("Proveedor apis.net.pe sin token (app.reniec.fallback.token); se omite.");
            return false;
        }
        return true;
    }

    @Override
    public Optional<ReniecConsultaResponse> consultar(String dni) {
        try {
            ApisNetPeApiResponse resp = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("numero", dni)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(ApisNetPeApiResponse.class);

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
            if (status == 429) {
                throw new DniProviderException("límite de consultas excedido (HTTP 429)", e);
            }
            throw new DniProviderException("respuesta de error HTTP " + status, e);
        } catch (RestClientException e) {
            throw new DniProviderException("fallo de comunicación: " + e.getMessage(), e);
        }
    }

    private ReniecConsultaResponse toResponse(String dni, ApisNetPeApiResponse resp) {
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
