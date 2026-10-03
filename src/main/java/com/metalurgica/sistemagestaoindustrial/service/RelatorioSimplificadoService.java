package com.metalurgica.sistemagestaoindustrial.service;

import com.metalurgica.sistemagestaoindustrial.analytics.AnalyticsQuery;
import com.metalurgica.sistemagestaoindustrial.analytics.AnalyticsService;
import com.metalurgica.sistemagestaoindustrial.dto.response.RelatorioSimplificadoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RelatorioSimplificadoService {
    private final AnalyticsService analytics;

    public RelatorioSimplificadoService(AnalyticsService analytics) { this.analytics = analytics; }

    @Transactional(readOnly = true)
    public RelatorioSimplificadoResponse generate(Long empresaId, AnalyticsQuery query) {
        var dashboard = analytics.coreDashboard(empresaId, query);
        var resumo = "Periodo " + query.inicio() + " a " + query.fim() + ": " + dashboard.quantidadeProduzida()
                + " unidades produzidas em " + dashboard.setoresComDados() + " setor(es).";
        return new RelatorioSimplificadoResponse(dashboard, resumo);
    }
}
