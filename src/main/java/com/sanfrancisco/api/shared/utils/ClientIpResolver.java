package com.sanfrancisco.api.shared.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resuelve la IP real del cliente para rate limiting, protección de fuerza
 * bruta y auditoría.
 * <p>
 * Con {@code server.forward-headers-strategy=framework} activo, Spring resuelve
 * {@code X-Forwarded-For} enviado por el proxy de confianza (Railway) y
 * {@code getRemoteAddr()} ya devuelve la IP original del cliente. Leer el header
 * a mano (como se hacía antes en cada filtro) permitía a cualquier cliente
 * suplantar su IP y evadir el rate limit o bloquear IPs ajenas.
 */
public final class ClientIpResolver {

    private ClientIpResolver() {
    }

    public static String resolve(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
