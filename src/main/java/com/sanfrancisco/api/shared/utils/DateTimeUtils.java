package com.sanfrancisco.api.shared.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class DateTimeUtils {
    private static final ZoneId PERU_ZONE = ZoneId.of("America/Lima");

    private DateTimeUtils() {
    }

    public static LocalDate today() {
        return LocalDate.now(PERU_ZONE);
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(PERU_ZONE);
    }
}
