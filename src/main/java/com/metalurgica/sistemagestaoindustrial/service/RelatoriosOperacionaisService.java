package com.metalurgica.sistemagestaoindustrial.service;

import com.metalurgica.sistemagestaoindustrial.analytics.AnalyticsQuery;
import com.metalurgica.sistemagestaoindustrial.analytics.AnalyticsService;
import com.metalurgica.sistemagestaoindustrial.analytics.Dimension;
import com.metalurgica.sistemagestaoindustrial.dto.response.*;
import com.metalurgica.sistemagestaoindustrial.report.ReportQuery;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RelatoriosOperacionaisService {
    private final AnalyticsService analytics;
    private final DesempenhoService desempenho;

    public RelatoriosOperacionaisService(AnalyticsService analytics, DesempenhoService desempenho) {
        this.analytics = analytics;
        this.desempenho = desempenho;
    }

    @Transactional(readOnly = true)
    public RelatorioProducaoResponse production(Long empresaId, ReportQuery q) {
        var query = analyticsQuery(q);
        var page = PageRequest.of(0, q.top() == null ? 10 : q.top());
        return new RelatorioProducaoResponse(analytics.summary(empresaId, query),
            analytics.grouped(empresaId, query, Dimension.SETOR, page).getContent(),
            analytics.grouped(empresaId, query, Dimension.FUNCIONARIO, page).getContent(),
            analytics.grouped(empresaId, query, Dimension.PECA, page).getContent(),
            analytics.grouped(empresaId, query, Dimension.MAQUINA, page).getContent(),
            analytics.grouped(empresaId, query, Dimension.TURNO, page).getContent());
    }

    @Transactional(readOnly = true)
    public RelatorioParadasResponse stops(Long empresaId, ReportQuery q) {
        var query = analyticsQuery(q);
        var summary = analytics.summary(empresaId, query);
        var reasons = analytics.stopReasons(empresaId, query, PageRequest.of(0, q.top() == null ? 10 : q.top())).getContent();
        long occurrences = reasons.stream().mapToLong(ParadaPorMotivoResponse::quantidadeOcorrencias).sum();
        return new RelatorioParadasResponse(summary.tempoParadoMinutos(), summary.percentualTempoParado(), occurrences, reasons);
    }

    @Transactional(readOnly = true)
    public RelatorioDesempenhoResponse performance(Long empresaId, ReportQuery q) {
        var query = analyticsQuery(q);
        var page = PageRequest.of(0, q.top() == null ? 10 : q.top());
        return new RelatorioDesempenhoResponse(desempenho.summary(empresaId, query),
            desempenho.compare(empresaId, query, Dimension.SETOR, page).getContent(),
            desempenho.compare(empresaId, query, Dimension.FUNCIONARIO, page).getContent());
    }

    private AnalyticsQuery analyticsQuery(ReportQuery q) {
        return new AnalyticsQuery(q.atual().inicio(), q.atual().fim(), q.setorId(), q.funcionarioId(), q.pecaId(), q.maquinaId(), q.turnoId());
    }
}
