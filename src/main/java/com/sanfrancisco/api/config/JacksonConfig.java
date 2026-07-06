package com.sanfrancisco.api.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.sanfrancisco.api.shared.utils.LenientLocalDateTimeDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * Ajustes de Jackson que se suman a la autoconfiguración de Spring Boot
 * (JavaTimeModule, formato ISO, FAIL_ON_UNKNOWN=false, etc.).
 *
 * Registra un deserializador tolerante para LocalDateTime que acepta tanto
 * fecha-hora ISO como fecha sola (normalizada a medianoche). Se expone como un
 * bean Module: Spring Boot registra automáticamente todos los Module en el
 * ObjectMapper. Solo afecta a la deserialización de cuerpos JSON; la
 * serialización de respuestas no cambia.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public SimpleModule lenientDateTimeModule() {
        SimpleModule module = new SimpleModule("LenientLocalDateTimeModule");
        module.addDeserializer(LocalDateTime.class, new LenientLocalDateTimeDeserializer());
        return module;
    }
}
