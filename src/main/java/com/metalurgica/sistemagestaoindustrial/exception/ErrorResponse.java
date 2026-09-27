package com.metalurgica.sistemagestaoindustrial.exception;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorResponse(OffsetDateTime timestamp, int status, String error, String message,
                            String path, Map<String, String> fields) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(OffsetDateTime.now(), status, error, message, path, Map.of());
    }
}
