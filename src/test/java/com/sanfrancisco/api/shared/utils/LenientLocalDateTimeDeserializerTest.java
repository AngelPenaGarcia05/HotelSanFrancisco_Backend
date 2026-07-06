package com.sanfrancisco.api.shared.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDateTime.class, new LenientLocalDateTimeDeserializer());
        mapper = new ObjectMapper().registerModule(module);
    }

    @Test
    @DisplayName("acepta fecha-hora ISO completa")
    void parseaFechaHora() throws Exception {
        Wrapper result = mapper.readValue("{\"fecha\":\"2026-07-06T10:30:00\"}", Wrapper.class);
        assertThat(result.fecha()).isEqualTo(LocalDateTime.of(2026, 7, 6, 10, 30, 0));
    }

    @Test
    @DisplayName("acepta fecha sola y la normaliza a medianoche")
    void parseaFechaSola() throws Exception {
        Wrapper result = mapper.readValue("{\"fecha\":\"2026-07-06\"}", Wrapper.class);
        assertThat(result.fecha()).isEqualTo(LocalDateTime.of(2026, 7, 6, 0, 0, 0));
    }

    @Test
    @DisplayName("null se deserializa como null")
    void parseaNull() throws Exception {
        Wrapper result = mapper.readValue("{\"fecha\":null}", Wrapper.class);
        assertThat(result.fecha()).isNull();
    }

    @Test
    @DisplayName("un texto inválido relanza el error de fecha-hora")
    void rechazaTextoInvalido() {
        assertThatThrownBy(() -> mapper.readValue("{\"fecha\":\"no-es-fecha\"}", Wrapper.class))
                .isInstanceOf(InvalidFormatException.class);
    }
}
