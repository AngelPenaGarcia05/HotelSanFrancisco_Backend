package com.sanfrancisco.api.shared.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("LenientLocalDateTimeDeserializer")
class LenientLocalDateTimeDeserializerTest {

    private record Wrapper(LocalDateTime fecha) {
    }

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        SimpleModule module = new SimpleModule("LenientLocalDateTimeModule");
        module.addDeserializer(LocalDateTime.class, new LenientLocalDateTimeDeserializer());
        mapper = JsonMapper.builder().addModule(module).build();
    }

    @Test
    @DisplayName("acepta fecha-hora ISO completa")
    void parseaFechaHora() {
        Wrapper result = mapper.readValue("{\"fecha\":\"2026-07-06T10:30:00\"}", Wrapper.class);
        assertThat(result.fecha()).isEqualTo(LocalDateTime.of(2026, 7, 6, 10, 30, 0));
    }

    @Test
    @DisplayName("acepta fecha sola y la normaliza a medianoche")
    void parseaFechaSola() {
        Wrapper result = mapper.readValue("{\"fecha\":\"2026-07-06\"}", Wrapper.class);
        assertThat(result.fecha()).isEqualTo(LocalDateTime.of(2026, 7, 6, 0, 0, 0));
    }

    @Test
    @DisplayName("null se deserializa como null")
    void parseaNull() {
        Wrapper result = mapper.readValue("{\"fecha\":null}", Wrapper.class);
        assertThat(result.fecha()).isNull();
    }

    @Test
    @DisplayName("un texto inválido se reporta como valor inválido")
    void rechazaTextoInvalido() {
        assertThatThrownBy(() -> mapper.readValue("{\"fecha\":\"no-es-fecha\"}", Wrapper.class))
                .isInstanceOf(InvalidFormatException.class);
    }
}
