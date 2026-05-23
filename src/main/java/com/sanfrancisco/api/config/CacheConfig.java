package com.sanfrancisco.api.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    // Simple in-memory cache (ConcurrentHashMap) configured via application.yaml
    // Replace with Caffeine or Redis when scaling requirements demand it
}
