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
                "proveedores", "jwtBlacklist", "bruteForce", "reniecDni"
        ));
        manager.setAsyncCacheMode(false);
        return manager;
    }
}
