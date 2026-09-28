package com.metalurgica.sistemagestaoindustrial.report;

public record ReportComparison(ReportPeriod atual, ReportPeriod anterior) {
    public ReportComparison {
        if (atual == null) throw new IllegalArgumentException("Periodo atual e obrigatorio.");
        if (anterior == null) anterior = atual.previous();
    }

    public static ReportComparison of(ReportPeriod atual, ReportPeriod anterior) {
        return new ReportComparison(atual, anterior);
    }
}
