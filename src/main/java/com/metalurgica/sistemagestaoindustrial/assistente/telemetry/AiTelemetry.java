package com.metalurgica.sistemagestaoindustrial.assistente.telemetry;

import com.metalurgica.sistemagestaoindustrial.observability.OperationalMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AiTelemetry {
    private static final Logger LOG = LoggerFactory.getLogger(AiTelemetry.class);
    private final OperationalMetrics metrics;

    public AiTelemetry(OperationalMetrics metrics) { this.metrics = metrics; }

    public void request(long empresaId, long userId, String provider, String model,
                        long latencyMs, int inputTokens, int outputTokens, int toolCalls, String outcome) {
        LOG.info("ai_request provider={} model={} latencyMs={} inputTokens={} outputTokens={} toolCalls={} outcome={}",
                safe(provider), safe(model), latencyMs, inputTokens, outputTokens, toolCalls, safe(outcome));
        metrics.ai(OperationalMetrics.Outcome.classify(outcome), latencyMs, inputTokens, outputTokens, toolCalls);
    }

    private String safe(String value) { return value == null ? "unknown" : value.replaceAll("[^A-Za-z0-9_.-]", "_"); }
}
