package com.metalurgica.sistemagestaoindustrial.report;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ReportQueryFactory {
    private final ReportLimitsProperties limits;

    public ReportQueryFactory(ReportLimitsProperties limits) { this.limits = limits; }

    public ReportQuery create(LocalDateTime inicio, LocalDateTime fim, LocalDateTime inicioAnterior,
                              LocalDateTime fimAnterior, Long setorId, Long funcionarioId, Long pecaId,
                              Long maquinaId, Long turnoId, Integer top) {
        var current = new ReportPeriod(inicio, fim);
        limits.validatePeriod(current);
        ReportPeriod previous = inicioAnterior == null && fimAnterior == null ? null : new ReportPeriod(inicioAnterior, fimAnterior);
        if (previous != null) limits.validatePeriod(previous);
        limits.validateTop(top);
        return new ReportQuery(ReportComparison.of(current, previous), setorId, funcionarioId, pecaId, maquinaId, turnoId, top);
    }
}
