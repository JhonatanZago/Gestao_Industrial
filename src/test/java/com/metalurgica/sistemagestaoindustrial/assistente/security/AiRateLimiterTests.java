package com.metalurgica.sistemagestaoindustrial.assistente.security;

import com.metalurgica.sistemagestaoindustrial.assistente.config.AiProperties;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AiRateLimiterTests {
    @Test
    void limitaPorUsuarioEEmpresa() {
        var properties = new AiProperties();
        properties.setMaxRequestsPerMinute(1);
        var limiter = new AiRateLimiter(properties);
        var context = new AssistenteSecurityContext(1L, 2L, "OPERADOR", Set.of(), Set.of(), true);
        limiter.acquire(context);
        assertThrows(RuntimeException.class, () -> limiter.acquire(context));
    }
}
