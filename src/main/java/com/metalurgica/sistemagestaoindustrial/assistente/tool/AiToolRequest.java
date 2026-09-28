package com.metalurgica.sistemagestaoindustrial.assistente.tool;

import com.metalurgica.sistemagestaoindustrial.assistente.security.AssistenteSecurityContext;
import org.springframework.security.core.Authentication;

import java.util.Map;

public record AiToolRequest(
        String name,
        Map<String, Object> arguments,
        AssistenteSecurityContext securityContext,
        Authentication authentication,
        int callNumber) {
    public AiToolRequest {
        arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
        if (callNumber < 1) throw new IllegalArgumentException("Número de chamada de tool inválido.");
    }
}
