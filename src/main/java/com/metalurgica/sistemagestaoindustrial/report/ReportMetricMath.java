package com.metalurgica.sistemagestaoindustrial.report;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ReportMetricMath {
    private ReportMetricMath() { }

    public static BigDecimal attainment(BigDecimal actual, BigDecimal goal) {
        return actual == null || goal == null || goal.signum() == 0 ? null : actual.multiply(BigDecimal.valueOf(100)).divide(goal, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal variationPercent(BigDecimal current, BigDecimal previous) {
        return previous == null || previous.signum() == 0 ? null : current.subtract(previous).divide(previous, 8, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
    }
}
