package com.sanfrancisco.api.config;

import com.sanfrancisco.api.shared.utils.LenientLocalDateTimeDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDateTime;

/**
 * Ajustes de Jackson 3 (tools.jackson, el que usa Spring Boot 4 para los
 * bodies HTTP) que se suman a la autoconfiguración.
 *
 * Registra un deserializador tolerante para LocalDateTime que acepta tanto
 * fecha-hora ISO como fecha sola (normalizada a medianoche). Se expone como
 * bean JacksonModule: Spring Boot registra automáticamente todos los módulos
 * en el ObjectMapper. Solo afecta a la deserialización de cuerpos JSON; la
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
