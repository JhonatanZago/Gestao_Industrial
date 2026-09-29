package com.metalurgica.sistemagestaoindustrial.observability;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.Locale;

@Component
public class OperationalMetrics {
    public enum Outcome { SUCCESS, REJECTED, FAILED, TIMEOUT, UNAVAILABLE, DISABLED, RATE_LIMITED, NO_DATA, CLARIFICATION, ERROR;
        public static Outcome classify(String value) {
            if (value == null) return ERROR;
            String v = value.toUpperCase(Locale.ROOT);
            if (v.contains("TIMEOUT")) return TIMEOUT; if (v.contains("UNAVAILABLE") || v.contains("INDISPONIVEL")) return UNAVAILABLE;
            if (v.contains("DISABLED")) return DISABLED; if (v.contains("RATE")) return RATE_LIMITED; if (v.contains("NO_DATA") || v.contains("SEM_DADOS")) return NO_DATA;
            if (v.contains("CLAR")) return CLARIFICATION; if (v.contains("RESPONDIDO") || v.contains("SUCCESS") || v.contains("CONCLUID")) return SUCCESS;
            if (v.contains("REJEIT")) return REJECTED; if (v.contains("FAIL") || v.contains("ERRO")) return FAILED; return ERROR;
        }
    }
    public enum ReportFormatTag { JSON, CSV, XLSX }
    private final MeterRegistry registry;
    public OperationalMetrics(MeterRegistry registry) { this.registry = registry; }
    public long start() { return System.nanoTime(); }
    public void importStarted() { registry.counter("imports.started").increment(); }
    public void importFinished(Outcome outcome, long started, int lines) {
        registry.counter(outcome == Outcome.SUCCESS ? "imports.completed" : "imports.failed", "outcome", outcome.name().toLowerCase(Locale.ROOT)).increment();
        Timer.builder("import.duration").tag("outcome", outcome.name().toLowerCase(Locale.ROOT)).register(registry).record(Duration.ofNanos(System.nanoTime() - started));
        DistributionSummary.builder("import.lines").tag("outcome", outcome.name().toLowerCase(Locale.ROOT)).register(registry).record(Math.max(0, lines));
    }
    public void report(ReportFormatTag format, Outcome outcome, long started) {
        registry.counter("report.generation", "format", format.name().toLowerCase(Locale.ROOT), "outcome", outcome.name().toLowerCase(Locale.ROOT)).increment();
        if (outcome != Outcome.SUCCESS) registry.counter("report.generation.failures", "format", format.name().toLowerCase(Locale.ROOT)).increment();
        Timer.builder("report.generation.duration").tags("format", format.name().toLowerCase(Locale.ROOT), "outcome", outcome.name().toLowerCase(Locale.ROOT)).register(registry).record(Duration.ofNanos(System.nanoTime() - started));
    }
    public void dashboard(Outcome outcome, long started) {
        registry.counter("dashboard.requests", "outcome", outcome.name().toLowerCase(Locale.ROOT)).increment();
        Timer.builder("dashboard.request.duration").tag("outcome", outcome.name().toLowerCase(Locale.ROOT)).register(registry).record(Duration.ofNanos(System.nanoTime() - started));
    }
    public void ai(Outcome outcome, long latencyMs, int inputTokens, int outputTokens, int toolCalls) {
        registry.counter("ai.requests", "outcome", outcome.name().toLowerCase(Locale.ROOT)).increment();
        if (outcome != Outcome.SUCCESS && outcome != Outcome.NO_DATA && outcome != Outcome.CLARIFICATION) registry.counter("ai.failures", "type", outcome.name().toLowerCase(Locale.ROOT)).increment();
        Timer.builder("ai.request.duration").tag("outcome", outcome.name().toLowerCase(Locale.ROOT)).register(registry).record(Duration.ofMillis(Math.max(0, latencyMs)));
        registry.counter("ai.tool.calls").increment(Math.max(0, toolCalls));
        DistributionSummary.builder("ai.tokens").tag("direction", "input").register(registry).record(Math.max(0, inputTokens));
        DistributionSummary.builder("ai.tokens").tag("direction", "output").register(registry).record(Math.max(0, outputTokens));
    }
}

