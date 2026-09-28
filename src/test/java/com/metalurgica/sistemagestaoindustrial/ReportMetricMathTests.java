package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.report.ReportMetricMath;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportMetricMathTests {
    @Test
    void calculatesAcceptanceAndComparativePercentagesDeterministically() {
        assertEquals(new BigDecimal("90.91"), ReportMetricMath.attainment(BigDecimal.valueOf(10000), BigDecimal.valueOf(11000)));
        assertEquals(new BigDecimal("25.00"), ReportMetricMath.variationPercent(BigDecimal.valueOf(10000), BigDecimal.valueOf(8000)));
        assertEquals(new BigDecimal("21.43"), ReportMetricMath.variationPercent(BigDecimal.valueOf(8500), BigDecimal.valueOf(7000)));
        assertEquals(new BigDecimal("-25.00"), ReportMetricMath.variationPercent(BigDecimal.valueOf(60), BigDecimal.valueOf(80)));
    }
}
