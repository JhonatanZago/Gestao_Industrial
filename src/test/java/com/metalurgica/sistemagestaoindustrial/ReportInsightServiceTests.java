package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.dto.response.CoberturaDesempenhoResponse;
import com.metalurgica.sistemagestaoindustrial.dto.response.DesempenhoResponse;
import com.metalurgica.sistemagestaoindustrial.dto.response.ResumoProducaoResponse;
import com.metalurgica.sistemagestaoindustrial.report.ReportInsightService;
import com.metalurgica.sistemagestaoindustrial.report.ReportLimitsProperties;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportInsightServiceTests {
    @Test
    void createsOnlyObjectiveCoverageAndStoppedTimeMessages() {
        var limits = new ReportLimitsProperties();
        limits.setInsightVariationPercent(20);
        limits.setStandardCoverageThresholdPercent(80);
        var periodStart = LocalDateTime.of(2026, 9, 1, 0, 0);
        var periodEnd = LocalDateTime.of(2026, 9, 1, 10, 0);
        var production = new ResumoProducaoResponse(periodStart, periodEnd, BigDecimal.TEN, 1, BigDecimal.valueOf(600), BigDecimal.valueOf(120), BigDecimal.valueOf(480), null, null, BigDecimal.valueOf(20));
        var performance = new DesempenhoResponse(periodStart, periodEnd, BigDecimal.TEN, BigDecimal.ONE, BigDecimal.TEN, null, null, false, new CoberturaDesempenhoResponse(10, 6, 4, BigDecimal.valueOf(6), BigDecimal.valueOf(60)));
        var points = new ReportInsightService(limits).attentionPoints(production, performance);
        assertEquals(2, points.size());
        assertEquals("A cobertura de tempo padrao no periodo foi de 60%.", points.get(1));
    }
}
