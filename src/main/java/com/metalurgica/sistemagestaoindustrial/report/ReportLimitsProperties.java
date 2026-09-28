package com.metalurgica.sistemagestaoindustrial.report;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "reports")
public class ReportLimitsProperties {
    private long maxPeriodDays = 366;
    private int maxTop = 100;
    private int maxRows = 10000;
    private long insightVariationPercent = 20;
    private long standardCoverageThresholdPercent = 80;

    @PostConstruct
    void validate() {
        if (maxPeriodDays < 1 || maxTop < 1 || maxRows < 1 || insightVariationPercent < 0 || standardCoverageThresholdPercent < 0 || standardCoverageThresholdPercent > 100) {
            throw new IllegalStateException("Configuracao de limites de relatorios invalida.");
        }
    }

    public void validatePeriod(ReportPeriod period) {
        if (period.duration().compareTo(Duration.ofDays(maxPeriodDays)) > 0) {
            throw new IllegalArgumentException("Periodo do relatorio excede o limite configurado.");
        }
    }

    public void validateTop(Integer top) {
        if (top != null && top > maxTop) throw new IllegalArgumentException("Top excede o limite configurado.");
    }

    public long getMaxPeriodDays() { return maxPeriodDays; }
    public void setMaxPeriodDays(long value) { maxPeriodDays = value; }
    public int getMaxTop() { return maxTop; }
    public void setMaxTop(int value) { maxTop = value; }
    public int getMaxRows() { return maxRows; }
    public void setMaxRows(int value) { maxRows = value; }
    public long getInsightVariationPercent() { return insightVariationPercent; }
    public void setInsightVariationPercent(long value) { insightVariationPercent = value; }
    public long getStandardCoverageThresholdPercent() { return standardCoverageThresholdPercent; }
    public void setStandardCoverageThresholdPercent(long value) { standardCoverageThresholdPercent = value; }
}
