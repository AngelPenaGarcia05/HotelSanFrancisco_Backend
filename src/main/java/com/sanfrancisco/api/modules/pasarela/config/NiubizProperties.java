package com.sanfrancisco.api.modules.pasarela.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Credenciales y endpoints de Niubiz. Todo se inyecta por variables de entorno;
 * los defaults apuntan al ambiente de test (apitestenv). Las credenciales nunca se loguean.
 */
@Component
public class NiubizProperties {

    private final boolean enabled;
    private final String baseUrl;
    private final String merchantId;
    private final String user;
    private final String password;
    private final String checkoutScriptUrl;
    private final int sessionExpirationMinutes;
    private final boolean reuseSecurityToken;

    public NiubizProperties(
            @Value("${app.niubiz.enabled:false}") boolean enabled,
            @Value("${app.niubiz.base-url:https://apitestenv.vnforapps.com}") String baseUrl,
            @Value("${app.niubiz.merchant-id:}") String merchantId,
            @Value("${app.niubiz.user:}") String user,
            @Value("${app.niubiz.password:}") String password,
            @Value("${app.niubiz.checkout-script-url:https://static-content-qas.vnforapps.com/v2/js/checkout.js?qa=true}") String checkoutScriptUrl,
            @Value("${app.niubiz.session-expiration-minutes:15}") int sessionExpirationMinutes,
            // El sandbox compartido de integración invalida el security token tras un
            // uso ("Token has been used before"); con credenciales propias puede
            // reusarse ~1 h activando este flag.
            @Value("${app.niubiz.reuse-security-token:false}") boolean reuseSecurityToken) {
        this.enabled = enabled;
        this.baseUrl = baseUrl;
        this.merchantId = merchantId;
        this.user = user;
        this.password = password;
        this.checkoutScriptUrl = checkoutScriptUrl;
        this.sessionExpirationMinutes = sessionExpirationMinutes;
        this.reuseSecurityToken = reuseSecurityToken;
    }

    public boolean isReuseSecurityToken() {
        return reuseSecurityToken;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isConfigured() {
        return enabled && !merchantId.isBlank() && !user.isBlank() && !password.isBlank();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getCheckoutScriptUrl() {
        return checkoutScriptUrl;
    }

    public int getSessionExpirationMinutes() {
        return sessionExpirationMinutes;
    }
}
