package com.sanfrancisco.api.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuración de Caffeine como proveedor principal de cache.
 * La especificación global se toma de application.yaml (spring.cache.caffeine.spec),
 * pero exponemos el bean explícitamente para permitir overrides programáticos
 * (p.ej. caches de catálogo con TTL más largo).
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CaffeineCacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(15))
                .recordStats());
        manager.setCacheNames(java.util.List.of(
                "tiposHabitacion", "habitaciones", "canales", "tiposDocumento",
                "metodosPago", "tiposServicio", "categoriasProducto", "horarios",
                "proveedores", "reniecDni"
        ));

        // Caches de seguridad con política propia, separados de los catálogos:
        // si compartieran el maximumSize(1000) global, bastaría con llenar el cache
        // para desalojar entradas y que un token revocado vuelva a ser válido o un
        // bloqueo de fuerza bruta se libere.
        //
        // jwtBlacklist / revokedUsers: TTL = vida del access token (15 min); pasada
        // esa ventana el token expira por sí solo y la entrada ya no hace falta.
        manager.registerCustomCache("jwtBlacklist", Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(15))
                .maximumSize(100_000)
                .build());
        manager.registerCustomCache("revokedUsers", Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(15))
                .maximumSize(100_000)
                .build());
        // bruteForce: ventana de 15 min desde el último intento fallido.
        manager.registerCustomCache("bruteForce", Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(15))
                .maximumSize(100_000)
                .build());
        // rateLimit: las claves son por minuto; 2 min de TTL basta para purgarlas.
        manager.registerCustomCache("rateLimit", Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(2))
                .maximumSize(200_000)
                .build());

        manager.setAsyncCacheMode(false);
        return manager;
    }
}
