package com.metalurgica.sistemagestaoindustrial.exception;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorResponse(OffsetDateTime timestamp, int status, ErrorCode code, String message,
                            String path, String correlationId, Map<String, String> fields) {
    public ErrorResponse { fields = fields == null ? Map.of() : Map.copyOf(fields); }
    public static ErrorResponse of(int status, ErrorCode code, String message, String path, String correlationId) {
        return new ErrorResponse(OffsetDateTime.now(), status, code, message, path, correlationId, Map.of());
    }
}
