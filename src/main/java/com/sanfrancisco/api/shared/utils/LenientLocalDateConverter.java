package com.sanfrancisco.api.shared.utils;

import org.springframework.core.convert.converter.Converter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Converter tolerante de query params String → LocalDate.
 *
 * Acepta una fecha ISO ("2026-07-06") y también una fecha-hora ISO
 * ("2026-07-06T00:00:00"), de la que trunca la parte de hora. Es el gemelo de
 * {@link LenientLocalDateTimeDeserializer} pero para query params (los filtros
 * de búsqueda no pasan por Jackson sino por el ConversionService de Spring).
 *
 * Motivo: garantiza compatibilidad durante la transición de los filtros de
 * fecha a LocalDate — las pantallas del frontend que aún anexan
 * "T00:00:00"/"T23:59:59" siguen funcionando sin cambios.
 */
public class LenientLocalDateConverter implements Converter<String, LocalDate> {

    @Override
    public LocalDate convert(String source) {
        String text = source.trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException dateError) {
            // Si no es fecha sola, se intenta como fecha-hora y se trunca;
            // si tampoco lo es, se propaga para que Spring lo reporte como 400.
            try {
                return LocalDateTime.parse(text).toLocalDate();
            } catch (DateTimeParseException dateTimeError) {
                throw dateError;
            }
        }
    }
}
