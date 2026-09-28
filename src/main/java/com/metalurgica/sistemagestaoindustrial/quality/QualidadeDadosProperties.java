package com.metalurgica.sistemagestaoindustrial.quality;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "quality")
public class QualidadeDadosProperties {
    private Duration maxProductionDuration = Duration.ofHours(24);
    private Duration futureTolerance = Duration.ofDays(1);
    private Duration shortProductionDuration = Duration.ofMinutes(5);
    private BigDecimal suspiciousRatePerHour = new BigDecimal("10000");
    private BigDecimal suspiciousQuantity = new BigDecimal("100000");
    private BigDecimal coverageAlertThresholdPercent = new BigDecimal("80");
    private Duration duplicateTolerance = Duration.ZERO;
    private int maxPeriodDays = 366;
    private int maxPageSize = 100;
    private int maxRecords = 10_000;
    private int maxDuplicateGroups = 10_000;

    @PostConstruct
    void validate() {
        if (maxProductionDuration.isNegative() || futureTolerance.isNegative() || shortProductionDuration.isNegative()
                || duplicateTolerance.isNegative() || suspiciousRatePerHour.signum() <= 0
                || suspiciousQuantity.signum() <= 0 || coverageAlertThresholdPercent.signum() < 0
                || coverageAlertThresholdPercent.compareTo(BigDecimal.valueOf(100)) > 0
                || maxPeriodDays < 1 || maxPageSize < 1 || maxRecords < 1 || maxDuplicateGroups < 1) {
            throw new IllegalStateException("Configuração de qualidade de dados inválida.");
        }
    }

    public Duration getMaxProductionDuration() { return maxProductionDuration; }
    public void setMaxProductionDuration(Duration value) { maxProductionDuration = value; }
    public Duration getFutureTolerance() { return futureTolerance; }
    public void setFutureTolerance(Duration value) { futureTolerance = value; }
    public Duration getShortProductionDuration() { return shortProductionDuration; }
    public void setShortProductionDuration(Duration value) { shortProductionDuration = value; }
    public BigDecimal getSuspiciousRatePerHour() { return suspiciousRatePerHour; }
    public void setSuspiciousRatePerHour(BigDecimal value) { suspiciousRatePerHour = value; }
    public BigDecimal getSuspiciousQuantity() { return suspiciousQuantity; }
    public void setSuspiciousQuantity(BigDecimal value) { suspiciousQuantity = value; }
    public BigDecimal getCoverageAlertThresholdPercent() { return coverageAlertThresholdPercent; }
    public void setCoverageAlertThresholdPercent(BigDecimal value) { coverageAlertThresholdPercent = value; }
    public Duration getDuplicateTolerance() { return duplicateTolerance; }
    public void setDuplicateTolerance(Duration value) { duplicateTolerance = value; }
    public int getMaxPeriodDays() { return maxPeriodDays; }
    public void setMaxPeriodDays(int value) { maxPeriodDays = value; }
    public int getMaxPageSize() { return maxPageSize; }
    public void setMaxPageSize(int value) { maxPageSize = value; }
    public int getMaxRecords() { return maxRecords; }
    public void setMaxRecords(int value) { maxRecords = value; }
    public int getMaxDuplicateGroups() { return maxDuplicateGroups; }
    public void setMaxDuplicateGroups(int value) { maxDuplicateGroups = value; }
}
