package com.metalurgica.sistemagestaoindustrial.observability;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class OperationalMetricsTests {
    @Test void recordsStableLowCardinalityMeters() {
        var registry = new SimpleMeterRegistry(); var metrics = new OperationalMetrics(registry);
        long started = metrics.start(); metrics.importStarted(); metrics.importFinished(OperationalMetrics.Outcome.SUCCESS, started, 10);
        metrics.report(OperationalMetrics.ReportFormatTag.XLSX, OperationalMetrics.Outcome.SUCCESS, metrics.start());
        metrics.dashboard(OperationalMetrics.Outcome.FAILED, metrics.start()); metrics.ai(OperationalMetrics.Outcome.TIMEOUT, 20, 3, 4, 1);
        assertEquals(1, registry.get("imports.started").counter().count());
        assertEquals(1, registry.get("report.generation").tag("format", "xlsx").counter().count());
        assertEquals(1, registry.get("ai.failures").tag("type", "timeout").counter().count());
        registry.getMeters().forEach(meter -> meter.getId().getTags().forEach(tag -> {
            assertFalse(tag.getKey().matches("empresa|user|usuario|arquivo|path|id"));
            assertFalse(tag.getValue().matches(".*[0-9]{6,}.*"));
        }));
    }
}
