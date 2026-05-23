package com.sanfrancisco.api.modules.seguridad.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class BruteForceProtectionService {

    private static final Logger log = LoggerFactory.getLogger(BruteForceProtectionService.class);
    private static final int MAX_ATTEMPTS = 5;

    private final CacheManager cacheManager;

    public BruteForceProtectionService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void loginFailed(String key) {
        Cache cache = cacheManager.getCache("bruteForce");
        if (cache == null) return;

        Integer attempts = cache.get(key, Integer.class);
        if (attempts == null) {
            attempts = 0;
        }
        attempts++;
        cache.put(key, attempts);
        log.warn("Intento fallido de login para {}. Intentos acumulados: {}", key, attempts);
    }

    public void loginSucceeded(String key) {
        Cache cache = cacheManager.getCache("bruteForce");
        if (cache != null) {
            cache.evict(key);
        }
    }

    public boolean isBlocked(String key) {
        Cache cache = cacheManager.getCache("bruteForce");
        if (cache == null) return false;

        Integer attempts = cache.get(key, Integer.class);
        if (attempts != null && attempts >= MAX_ATTEMPTS) {
            log.warn("El acceso para {} está bloqueado temporalmente por exceso de intentos fallidos", key);
            return true;
        }
        return false;
    }
}
