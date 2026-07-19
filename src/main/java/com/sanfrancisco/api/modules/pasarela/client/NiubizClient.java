package com.sanfrancisco.api.modules.pasarela.client;

import com.sanfrancisco.api.modules.pasarela.config.NiubizProperties;
import com.sanfrancisco.api.modules.pasarela.dto.NiubizAuthorizationResult;
import com.sanfrancisco.api.modules.pasarela.dto.NiubizSessionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

/**
 * Cliente HTTP del flujo e-commerce de Niubiz (Botón de Pago Web):
 *  1. security token (Basic auth, cacheado ~50 min),
 *  2. session token (monto + antifraude) para abrir el checkout,
 *  3. autorización con el transaction token que devuelve el checkout.
 *
 * Los datos de tarjeta nunca pasan por este backend: los captura el
 * formulario de Niubiz (checkout.js) en el navegador.
 */
@Component
public class NiubizClient {

    private static final Logger log = LoggerFactory.getLogger(NiubizClient.class);
    private static final String APPROVED_ACTION_CODE = "000";
    private static final Duration SECURITY_TOKEN_TTL = Duration.ofMinutes(50);

    private final NiubizProperties props;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    private volatile String cachedSecurityToken;
    private volatile long securityTokenExpiresAt;

    public NiubizClient(NiubizProperties props, ObjectMapper objectMapper) {
        this.props = props;
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(15));
        factory.setReadTimeout(Duration.ofSeconds(60));

        this.restClient = RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .requestFactory(factory)
                .build();
    }

    /**
     * Token de seguridad (credenciales del comercio). Con reuse-security-token
     * activo se cachea ~50 min; desactivado (default) se pide uno fresco por
     * operación, porque el sandbox compartido de Niubiz invalida el token tras
     * el primer uso ("Token has been used before").
     */
    public synchronized String obtenerSecurityToken() {
        if (props.isReuseSecurityToken()
                && cachedSecurityToken != null && System.currentTimeMillis() < securityTokenExpiresAt) {
            return cachedSecurityToken;
        }
        String basic = Base64.getEncoder().encodeToString(
                (props.getUser() + ":" + props.getPassword()).getBytes(StandardCharsets.UTF_8));
        try {
            String token = restClient.post()
                    .uri("/api.security/v1/security")
                    .header("Authorization", "Basic " + basic)
                    .retrieve()
                    .body(String.class);
            if (token == null || token.isBlank()) {
                throw new NiubizClientException("Niubiz devolvió un security token vacío");
            }
            cachedSecurityToken = token.trim();
            securityTokenExpiresAt = System.currentTimeMillis() + SECURITY_TOKEN_TTL.toMillis();
            return cachedSecurityToken;
        } catch (HttpStatusCodeException e) {
            throw new NiubizClientException("Credenciales Niubiz rechazadas (HTTP "
                    + e.getStatusCode().value() + ")", e);
        } catch (RestClientException e) {
            throw new NiubizClientException("Fallo de comunicación con Niubiz (security): " + e.getMessage(), e);
        }
    }

    /** Invalida el token cacheado para forzar uno nuevo en la siguiente operación. */
    private synchronized void invalidarSecurityToken() {
        cachedSecurityToken = null;
        securityTokenExpiresAt = 0;
    }

    /** Detecta errores de token (usado/expirado/no autorizado) en el body de error de Niubiz. */
    private boolean esErrorDeToken(String body) {
        if (body == null) return false;
        String b = body.toLowerCase();
        return b.contains("errormessage") && b.contains("token") && !b.contains("action_code");
    }

    /** Crea la sesión de checkout atada al monto exacto calculado por el backend. */
    public NiubizSessionData crearSesion(BigDecimal monto, String clientIp, String correoCliente) {
        try {
            return crearSesionInterno(monto, clientIp, correoCliente);
        } catch (HttpStatusCodeException e) {
            // Token consumido/expirado: se fuerza uno nuevo y se reintenta una vez.
            if (esErrorDeToken(e.getResponseBodyAsString())) {
                log.info("Security token Niubiz inválido al crear sesión; se renueva y reintenta");
                invalidarSecurityToken();
                try {
                    return crearSesionInterno(monto, clientIp, correoCliente);
                } catch (HttpStatusCodeException e2) {
                    log.warn("Niubiz session HTTP {} (reintento): {}", e2.getStatusCode().value(),
                            e2.getResponseBodyAsString());
                    throw new NiubizClientException("Niubiz rechazó la creación de sesión (HTTP "
                            + e2.getStatusCode().value() + ")", e2);
                }
            }
            log.warn("Niubiz session HTTP {}: {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new NiubizClientException("Niubiz rechazó la creación de sesión (HTTP "
                    + e.getStatusCode().value() + ")", e);
        }
    }

    private NiubizSessionData crearSesionInterno(BigDecimal monto, String clientIp, String correoCliente) {
        String securityToken = obtenerSecurityToken();

        ObjectNode antifraud = objectMapper.createObjectNode();
        antifraud.put("clientIp", clientIp != null ? clientIp : "0.0.0.0");
        ObjectNode mdd = antifraud.putObject("merchantDefineData");
        mdd.put("MDD4", correoCliente != null ? correoCliente : "");
        mdd.put("MDD21", 0);
        mdd.put("MDD32", correoCliente != null ? correoCliente : "");
        mdd.put("MDD75", "Invitado");
        mdd.put("MDD77", 1);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("channel", "web");
        body.put("amount", monto);
        body.set("antifraud", antifraud);

        try {
            JsonNode resp = restClient.post()
                    .uri("/api.ecommerce/v2/ecommerce/token/session/{merchantId}", props.getMerchantId())
                    .header("Authorization", securityToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body.toString())
                    .retrieve()
                    .body(JsonNode.class);
            if (resp == null || !resp.hasNonNull("sessionKey")) {
                throw new NiubizClientException("Niubiz no devolvió sessionKey al crear la sesión");
            }
            return new NiubizSessionData(
                    resp.get("sessionKey").asText(),
                    resp.hasNonNull("expirationTime") ? resp.get("expirationTime").asLong() : null);
        } catch (HttpStatusCodeException e) {
            // Se propaga para que crearSesion decida si renueva el token y reintenta.
            throw e;
        } catch (RestClientException e) {
            throw new NiubizClientException("Fallo de comunicación con Niubiz (session): " + e.getMessage(), e);
        }
    }

    /**
     * Autoriza la transacción con el token emitido por el checkout. Un rechazo
     * (Niubiz responde 4xx con dataMap) NO es excepción: se devuelve como
     * resultado no aprobado. Solo los fallos de comunicación lanzan
     * {@link NiubizClientException} — en ese caso el cobro queda indeterminado.
     */
    public NiubizAuthorizationResult autorizar(String transactionToken, String purchaseNumber,
                                               BigDecimal monto, String moneda) {
        String raw = ejecutarAutorizacion(transactionToken, purchaseNumber, monto, moneda);

        // Si el fallo fue del security token (no de la tarjeta), se renueva y
        // reintenta una vez: NO es un rechazo del pago y no debe registrarse como tal.
        if (esErrorDeToken(raw)) {
            log.info("Security token Niubiz inválido al autorizar; se renueva y reintenta");
            invalidarSecurityToken();
            raw = ejecutarAutorizacion(transactionToken, purchaseNumber, monto, moneda);
            if (esErrorDeToken(raw)) {
                throw new NiubizClientException("Niubiz no aceptó el security token en la autorización: " + raw);
            }
        }

        return parseAutorizacion(raw);
    }

    private String ejecutarAutorizacion(String transactionToken, String purchaseNumber,
                                        BigDecimal monto, String moneda) {
        String securityToken = obtenerSecurityToken();

        ObjectNode order = objectMapper.createObjectNode();
        order.put("tokenId", transactionToken);
        order.put("purchaseNumber", purchaseNumber);
        order.put("amount", monto);
        order.put("currency", moneda);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("channel", "web");
        body.put("captureType", "manual");
        body.put("countable", true);
        body.set("order", order);

        try {
            return restClient.post()
                    .uri("/api.authorization/v3/authorization/ecommerce/{merchantId}", props.getMerchantId())
                    .header("Authorization", securityToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body.toString())
                    .retrieve()
                    .body(String.class);
        } catch (HttpStatusCodeException e) {
            // Rechazo de negocio: Niubiz responde 4xx con el detalle en el body.
            String raw = e.getResponseBodyAsString();
            if (raw == null || raw.isBlank()) {
                throw new NiubizClientException("Niubiz devolvió HTTP "
                        + e.getStatusCode().value() + " sin detalle en la autorización", e);
            }
            return raw;
        } catch (RestClientException e) {
            throw new NiubizClientException("Fallo de comunicación con Niubiz (authorize): " + e.getMessage(), e);
        }
    }

    private NiubizAuthorizationResult parseAutorizacion(String raw) {
        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode dataMap = root.has("dataMap") && !root.get("dataMap").isNull()
                    ? root.get("dataMap")
                    : root.path("data");

            String actionCode = text(dataMap, "ACTION_CODE");
            String description = text(dataMap, "ACTION_DESCRIPTION");
            if (description == null) {
                description = text(dataMap, "STATUS");
            }
            String transactionId = text(dataMap, "TRANSACTION_ID");
            if (transactionId == null) {
                transactionId = text(root.path("order"), "transactionId");
            }

            return new NiubizAuthorizationResult(
                    APPROVED_ACTION_CODE.equals(actionCode),
                    actionCode,
                    description,
                    text(dataMap, "AUTHORIZATION_CODE"),
                    text(dataMap, "CARD"),
                    text(dataMap, "BRAND"),
                    transactionId,
                    raw);
        } catch (Exception e) {
            throw new NiubizClientException("No se pudo interpretar la respuesta de autorización de Niubiz", e);
        }
    }

    private String text(JsonNode node, String field) {
        return node != null && node.hasNonNull(field) ? node.get(field).asText() : null;
    }
}
