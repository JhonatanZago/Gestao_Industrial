package com.metalurgica.sistemagestaoindustrial.report;

public record ReportQuery(ReportComparison comparison, Long setorId, Long funcionarioId, Long pecaId,
                          Long maquinaId, Long turnoId, Integer top) {
    public ReportQuery {
        if (comparison == null) throw new IllegalArgumentException("Comparativo e obrigatorio.");
        if (top != null && top < 1) throw new IllegalArgumentException("Top deve ser positivo.");
    }

    public ReportPeriod atual() { return comparison.atual(); }
    public ReportPeriod anterior() { return comparison.anterior(); }
}
