package com.metalurgica.sistemagestaoindustrial.service;

import com.metalurgica.sistemagestaoindustrial.analytics.AnalyticsQuery;
import com.metalurgica.sistemagestaoindustrial.analytics.AnalyticsService;
import com.metalurgica.sistemagestaoindustrial.analytics.Dimension;
import com.metalurgica.sistemagestaoindustrial.analytics.FinanceiroQuery;
import com.metalurgica.sistemagestaoindustrial.domain.TipoMeta;
import com.metalurgica.sistemagestaoindustrial.dto.response.RelatorioExecutivoResponse;
import com.metalurgica.sistemagestaoindustrial.report.ReportQuery;
import com.metalurgica.sistemagestaoindustrial.report.ReportAuditLogger;
import com.metalurgica.sistemagestaoindustrial.report.ReportInsightService;
import com.metalurgica.sistemagestaoindustrial.report.ReportMetricMath;
import com.metalurgica.sistemagestaoindustrial.security.AuthorizationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class RelatorioExecutivoService {
    private final AnalyticsService analyticsService;
    private final FinanceiroIndustrialService financeiroService;
    private final DesempenhoService desempenhoService;
    private final MetaProducaoService metaService;
    private final EmpresaService empresaService;
    private final AuthorizationService authorizationService;
    private final ReportAuditLogger auditLogger;
    private final ReportInsightService insightService;

    public RelatorioExecutivoService(AnalyticsService analyticsService, FinanceiroIndustrialService financeiroService,
                                     DesempenhoService desempenhoService, MetaProducaoService metaService,
                                     EmpresaService empresaService, AuthorizationService authorizationService,
                                     ReportAuditLogger auditLogger, ReportInsightService insightService) {
        this.analyticsService = analyticsService;
        this.financeiroService = financeiroService;
        this.desempenhoService = desempenhoService;
        this.metaService = metaService;
        this.empresaService = empresaService;
        this.authorizationService = authorizationService;
        this.auditLogger = auditLogger;
        this.insightService = insightService;
    }

    @Transactional(readOnly = true)
    public RelatorioExecutivoResponse generate(Long empresaId, ReportQuery reportQuery) {
        var empresa = empresaService.get(empresaId);
        var query = analyticsQuery(reportQuery);
        var page = PageRequest.of(0, reportQuery.top() == null ? 10 : reportQuery.top());
        var production = analyticsService.summary(empresaId, query, true);
        var quality = production.qualidadeDados();
        var comparison = analyticsService.compare(empresaId, query, previousQuery(reportQuery));
        var sectors = analyticsService.grouped(empresaId, query, Dimension.SETOR, page).getContent();
        var employees = analyticsService.grouped(empresaId, query, Dimension.FUNCIONARIO, page).getContent();
        var parts = analyticsService.grouped(empresaId, query, Dimension.PECA, page).getContent();
        var machines = analyticsService.grouped(empresaId, query, Dimension.MAQUINA, page).getContent();
        var shifts = analyticsService.grouped(empresaId, query, Dimension.TURNO, page).getContent();
        var stops = analyticsService.stopReasons(empresaId, query, page).getContent();
        var goals = metaService.list(empresaId, null, true, page).getContent().stream()
            .map(meta -> new com.metalurgica.sistemagestaoindustrial.dto.response.ResumoMetaRelatorioResponse(meta, production.quantidadeProduzida(), attainment(production.quantidadeProduzida(), meta.quantidadeMeta()), status(production.quantidadeProduzida(), meta.quantidadeMeta())))
            .toList();
        var performance = desempenhoService.summary(empresaId, query);
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var financial = authorizationService.hasPermission(authentication, "FINANCEIRO_INDUSTRIAL_VISUALIZAR")
            ? financeiroService.summary(empresaId, financialQuery(reportQuery)) : null;
        var financialComparison = authorizationService.hasPermission(authentication, "FINANCEIRO_INDUSTRIAL_VISUALIZAR")
            ? financeiroService.compare(empresaId, financialQuery(reportQuery), previousFinancialQuery(reportQuery)) : null;
        var attentionPoints = new ArrayList<>(insightService.attentionPoints(production, performance));
        if (quality != null && quality.alertasCriticos() > 0) attentionPoints.add("Há alertas críticos de qualidade de dados no período.");
        if (quality != null && quality.duplicidades() > 0) attentionPoints.add("Há grupos de duplicidade de produção no período.");
        var response = new RelatorioExecutivoResponse(
            new RelatorioExecutivoResponse.Identificacao(empresa.id(), empresa.nomeFantasia(), reportQuery.atual().inicio(), reportQuery.atual().fim(), OffsetDateTime.now(), generator(authentication)),
            production, comparison, sectors, employees, parts, machines, shifts, stops, goals, performance, financial,
            attentionPoints, financialComparison, quality);
        auditLogger.generated(authentication, empresaId, "EXECUTIVO", reportQuery.atual(), "JSON");
        return response;
    }

    private String generator(org.springframework.security.core.Authentication authentication) {
        return authentication instanceof JwtAuthenticationToken jwt ? jwt.getToken().getSubject() : null;
    }

    private AnalyticsQuery analyticsQuery(ReportQuery q) {
        return new AnalyticsQuery(q.atual().inicio(), q.atual().fim(), q.setorId(), q.funcionarioId(), q.pecaId(), q.maquinaId(), q.turnoId());
    }

    private AnalyticsQuery previousQuery(ReportQuery q) {
        return new AnalyticsQuery(q.anterior().inicio(), q.anterior().fim(), q.setorId(), q.funcionarioId(), q.pecaId(), q.maquinaId(), q.turnoId());
    }

    private FinanceiroQuery financialQuery(ReportQuery q) {
        return new FinanceiroQuery(q.atual().inicio(), q.atual().fim(), q.setorId(), q.funcionarioId(), q.pecaId(), q.maquinaId(), q.turnoId(), null, null);
    }

    private FinanceiroQuery previousFinancialQuery(ReportQuery q) {
        return new FinanceiroQuery(q.anterior().inicio(), q.anterior().fim(), q.setorId(), q.funcionarioId(), q.pecaId(), q.maquinaId(), q.turnoId(), null, null);
    }

    private BigDecimal attainment(BigDecimal actual, BigDecimal goal) {
        return ReportMetricMath.attainment(actual, goal);
    }

    private String status(BigDecimal actual, BigDecimal goal) {
        var value = attainment(actual, goal);
        return value == null ? "SEM_DADOS" : value.compareTo(BigDecimal.valueOf(100)) >= 0 ? "ATINGIDA" : "ABAIXO_DA_META";
    }
}
