package com.metalurgica.sistemagestaoindustrial.assistente.telemetry;

import com.metalurgica.sistemagestaoindustrial.observability.OperationalMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiTelemetryTests {
    @Test
    void recordsTimeoutTokensAndToolsWithoutTenantOrUserTags() {
        var registry = new SimpleMeterRegistry();
        var telemetry = new AiTelemetry(new OperationalMetrics(registry));

        telemetry.request(123456789L, 987654321L, "remote-provider", "model-1",
                42, 10, 20, 2, "AI_TIMEOUT");

        assertEquals(1, registry.get("ai.requests").tag("outcome", "timeout").counter().count());
        assertEquals(1, registry.get("ai.failures").tag("type", "timeout").counter().count());
        assertEquals(2, registry.get("ai.tool.calls").counter().count());
        assertEquals(10, registry.get("ai.tokens").tag("direction", "input").summary().totalAmount());
        assertEquals(20, registry.get("ai.tokens").tag("direction", "output").summary().totalAmount());
        registry.getMeters().forEach(meter -> meter.getId().getTags().forEach(tag -> {
            assertNotEquals("123456789", tag.getValue());
            assertNotEquals("987654321", tag.getValue());
        }));
    }
}
