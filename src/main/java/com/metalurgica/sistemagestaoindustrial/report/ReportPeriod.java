package com.metalurgica.sistemagestaoindustrial.report;

import java.time.Duration;
import java.time.LocalDateTime;

public record ReportPeriod(LocalDateTime inicio, LocalDateTime fim) {
    public ReportPeriod {
        if (inicio == null || fim == null) throw new IllegalArgumentException("Inicio e fim do periodo sao obrigatorios.");
        if (fim.isBefore(inicio)) throw new IllegalArgumentException("Fim do periodo nao pode anteceder o inicio.");
    }

    public Duration duration() {
        return Duration.between(inicio, fim);
    }

    public ReportPeriod previous() {
        var duration = duration();
        return new ReportPeriod(inicio.minus(duration), inicio);
    }
}
