package com.sanfrancisco.api.shared.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("LenientLocalDateConverter")
class LenientLocalDateConverterTest {

    private final LenientLocalDateConverter converter = new LenientLocalDateConverter();

    @Test
    @DisplayName("acepta fecha ISO sola")
    void parseaFechaSola() {
        assertThat(converter.convert("2026-07-06")).isEqualTo(LocalDate.of(2026, 7, 6));
    }

    @Test
    @DisplayName("acepta fecha-hora ISO y trunca la hora")
    void parseaFechaHoraTruncando() {
        assertThat(converter.convert("2026-07-06T23:59:59")).isEqualTo(LocalDate.of(2026, 7, 6));
        assertThat(converter.convert("2026-07-06T00:00:00")).isEqualTo(LocalDate.of(2026, 7, 6));
    }

    @Test
    @DisplayName("string vacío se convierte en null (filtro ausente)")
    void vacioEsNull() {
        assertThat(converter.convert("  ")).isNull();
    }

    @Test
    @DisplayName("texto inválido lanza DateTimeParseException")
    void rechazaTextoInvalido() {
        assertThatThrownBy(() -> converter.convert("no-es-fecha"))
                .isInstanceOf(DateTimeParseException.class);
    }
}
