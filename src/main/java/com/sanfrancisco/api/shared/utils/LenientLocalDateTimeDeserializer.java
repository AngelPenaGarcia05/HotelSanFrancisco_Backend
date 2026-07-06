package com.sanfrancisco.api.shared.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.io.IOException;
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
 * enviar una fecha sola provoca un 500 de Jackson ("could not be parsed at index 10").
 *
 * Si el texto no es ni fecha-hora ni fecha válida, se relanza el error original
 * para no ocultar entradas realmente inválidas.
 */
public class LenientLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
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
                throw new InvalidFormatException(parser,
                        "No se pudo interpretar '" + text + "' como fecha-hora ni como fecha (esperado ISO, p.ej. 2026-07-06 o 2026-07-06T10:30:00)",
                        text, LocalDateTime.class);
            }
        }
    }
}
