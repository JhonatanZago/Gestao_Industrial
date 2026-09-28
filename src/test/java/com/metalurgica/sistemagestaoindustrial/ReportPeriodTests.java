package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.report.ReportComparison;
import com.metalurgica.sistemagestaoindustrial.report.ReportLimitsProperties;
import com.metalurgica.sistemagestaoindustrial.report.ReportPeriod;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReportPeriodTests {
    private static final LocalDateTime START = LocalDateTime.of(2026, 9, 1, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 9, 30, 23, 59);

    @Test
    void derivesPreviousPeriodWithTheSameDuration() {
        var current = new ReportPeriod(START, END);
        var comparison = ReportComparison.of(current, null);
        assertEquals(START.minus(current.duration()), comparison.anterior().inicio());
        assertEquals(START, comparison.anterior().fim());
    }

    @Test
    void rejectsReversedPeriodAndExcessiveTop() {
        assertThrows(IllegalArgumentException.class, () -> new ReportPeriod(END, START));
        var limits = new ReportLimitsProperties();
        limits.setMaxTop(10);
        assertThrows(IllegalArgumentException.class, () -> limits.validateTop(11));
    }

    @Test
    void rejectsPeriodBeyondConfiguredLimit() {
        var limits = new ReportLimitsProperties();
        limits.setMaxPeriodDays(1);
        assertThrows(IllegalArgumentException.class, () -> limits.validatePeriod(new ReportPeriod(START, END)));
    }
}
