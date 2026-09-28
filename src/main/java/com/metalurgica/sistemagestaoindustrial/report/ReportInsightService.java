package com.metalurgica.sistemagestaoindustrial.report;

import com.metalurgica.sistemagestaoindustrial.dto.response.DesempenhoResponse;
import com.metalurgica.sistemagestaoindustrial.dto.response.ResumoProducaoResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportInsightService {
    private final ReportLimitsProperties limits;
    public ReportInsightService(ReportLimitsProperties limits) { this.limits = limits; }

    public List<String> attentionPoints(ResumoProducaoResponse production, DesempenhoResponse performance) {
        var points = new ArrayList<String>();
        if (production.percentualTempoParado() != null && production.percentualTempoParado().compareTo(BigDecimal.valueOf(limits.getInsightVariationPercent())) >= 0) {
            points.add("O tempo parado representou " + production.percentualTempoParado() + "% do tempo bruto no periodo.");
        }
        if (performance.cobertura() != null && performance.cobertura().percentual() != null && performance.cobertura().percentual().compareTo(BigDecimal.valueOf(limits.getStandardCoverageThresholdPercent())) < 0) {
            points.add("A cobertura de tempo padrao no periodo foi de " + performance.cobertura().percentual() + "%.");
        }
        return points;
    }
}
