package com.sanfrancisco.api.config;

import com.sanfrancisco.api.shared.utils.LenientLocalDateConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Ajustes de Spring MVC. Registra el converter tolerante String → LocalDate
 * para query params (fecha sola o fecha-hora truncada); complementa al
 * deserializador tolerante de Jackson, que solo cubre los bodies JSON.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new LenientLocalDateConverter());
    }
}
