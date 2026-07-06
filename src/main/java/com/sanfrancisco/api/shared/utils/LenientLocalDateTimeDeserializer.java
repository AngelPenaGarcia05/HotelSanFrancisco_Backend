package com.sanfrancisco.api.shared.utils;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Deserializador tolerante de {@link LocalDateTime} para cuerpos JSON.
 *
 * Acepta el formato ISO fecha-hora habitual ("2026-07-06T10:30:00") y, además,
 * una fecha sola ("2026-07-06") — típica de un &lt;input type="date"&gt; del
 * frontend — normalizándola a medianoche (00:00:00).
 *
 * Motivo: varios campos de creación (ventas.fechaVenta, servicios.fechaConsumo,
 * pagos.fecha) son LocalDateTime que el cliente sí elige legítimamente. Sin esto,
 * enviar una fecha sola provoca un error de Jackson ("could not be parsed at index 10").
 *
 * Escrito contra la API de Jackson 3 (tools.jackson), que es la que Spring Boot 4
 * usa para los bodies HTTP; una versión Jackson 2 (com.fasterxml) nunca se registraría.
 *
 * Si el texto no es ni fecha-hora ni fecha válida, se reporta el valor como
 * inválido (400) para no ocultar entradas realmente erróneas.
 */
public class LenientLocalDateTimeDeserializer extends ValueDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) {
        String value = parser.getValueAsString();
        if (value == null || value.isBlank()) {
            return null;
        }
        String text = value.trim();
        try {
            return LocalDateTime.parse(text);
        } catch (DateTimeParseException dateTimeError) {
            try {
                return LocalDate.parse(text).atStartOfDay();
            } catch (DateTimeParseException dateError) {
                return (LocalDateTime) context.handleWeirdStringValue(LocalDateTime.class, text,
                        "No se pudo interpretar el valor como fecha-hora ni como fecha (esperado ISO, p.ej. 2026-07-06 o 2026-07-06T10:30:00)");
            }
        }
    }
}
