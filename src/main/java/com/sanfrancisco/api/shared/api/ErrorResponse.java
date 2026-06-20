package com.sanfrancisco.api.shared.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        boolean success,
        String code,
        String message,
        String path,
        Map<String, String> fieldErrors,
        List<String> details,
        Instant timestamp
) {

    public static ErrorResponse of(String code, String message, String path) {
        return new ErrorResponse(false, code, message, path, null, null, Instant.now());
    }

    public static ErrorResponse validation(String message, String path, Map<String, String> fieldErrors) {
        return new ErrorResponse(false, "VALIDATION_ERROR", message, path, fieldErrors, null, Instant.now());
    }
}
