package com.metalurgica.sistemagestaoindustrial.assistente.security;

import com.metalurgica.sistemagestaoindustrial.assistente.AssistenteErrorCode;
import com.metalurgica.sistemagestaoindustrial.assistente.config.AiProperties;
import com.metalurgica.sistemagestaoindustrial.assistente.tool.AiToolException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AiRateLimiter {
    private final AiProperties properties;
    private final Clock clock = Clock.systemUTC();
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public AiRateLimiter(AiProperties properties) {
        this.properties = properties;
    }

    public void acquire(AssistenteSecurityContext context) {
        var key = context.empresaId() + ":" + context.userId();
        var minute = clock.instant().getEpochSecond() / 60;
        var window = windows.compute(key, (ignored, current) -> {
            if (current == null || current.minute != minute) return new Window(minute, 1);
            return new Window(minute, current.count + 1);
        });
        if (window.count > properties.getMaxRequestsPerMinute()) {
            throw new AiToolException(AssistenteErrorCode.AI_RATE_LIMIT, "Limite de requisições de IA excedido.");
        }
    }

    private record Window(long minute, int count) { }
}
