package com.metalurgica.sistemagestaoindustrial.assistente.telemetry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AiTelemetry {
    private static final Logger LOG = LoggerFactory.getLogger(AiTelemetry.class);

    public void request(long empresaId, long userId, String provider, String model,
                        long latencyMs, int inputTokens, int outputTokens, int toolCalls, String outcome) {
        LOG.info("ai_request empresaId={} userId={} provider={} model={} latencyMs={} inputTokens={} outputTokens={} toolCalls={} outcome={}",
                empresaId, userId, safe(provider), safe(model), latencyMs, inputTokens, outputTokens, toolCalls, safe(outcome));
    }

    private String safe(String value) {
        return value == null ? "unknown" : value.replaceAll("[^A-Za-z0-9_.-]", "_");
    }
}
