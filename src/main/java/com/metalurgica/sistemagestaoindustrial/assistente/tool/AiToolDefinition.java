package com.metalurgica.sistemagestaoindustrial.assistente.tool;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

public record AiToolDefinition(
        String name,
        String version,
        String description,
        Map<String, Object> inputSchema,
        Set<String> requiredPermissions,
        boolean readOnly,
        Duration timeout,
        String sourceType) {
    public AiToolDefinition {
        if (name == null || name.isBlank() || version == null || version.isBlank()
                || description == null || description.isBlank() || timeout == null
                || timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("Definição de tool inválida.");
        }
        inputSchema = inputSchema == null ? Map.of() : Map.copyOf(inputSchema);
        requiredPermissions = requiredPermissions == null ? Set.of() : Set.copyOf(requiredPermissions);
    }
}
