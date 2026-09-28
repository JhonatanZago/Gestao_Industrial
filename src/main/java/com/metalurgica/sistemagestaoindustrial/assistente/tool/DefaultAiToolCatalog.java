package com.metalurgica.sistemagestaoindustrial.assistente.tool;

import com.metalurgica.sistemagestaoindustrial.analytics.*;
import com.metalurgica.sistemagestaoindustrial.assistente.AssistenteErrorCode;
import com.metalurgica.sistemagestaoindustrial.assistente.config.AiProperties;
import com.metalurgica.sistemagestaoindustrial.assistente.dto.*;
import com.metalurgica.sistemagestaoindustrial.assistente.security.AssistenteSecurityContext;
import com.metalurgica.sistemagestaoindustrial.audit.AuditoriaConsultaService;
import com.metalurgica.sistemagestaoindustrial.dto.request.AuditoriaFiltro;
import com.metalurgica.sistemagestaoindustrial.dto.response.QualidadeDadosResumoResponse;
import com.metalurgica.sistemagestaoindustrial.dto.response.ResumoProducaoResponse;
import com.metalurgica.sistemagestaoindustrial.quality.QualidadeDadosQuery;
import com.metalurgica.sistemagestaoindustrial.service.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Component
public class DefaultAiToolCatalog implements AiToolCatalog {
    private final AiProperties properties;
    private final AnalyticsService analyticsService;
    private final FinanceiroIndustrialService financeiroService;
    private final DesempenhoService desempenhoService;
    private final QualidadeDadosService qualidadeService;
    private final AuditoriaConsultaService auditoriaService;
    private final List<AiToolDefinition> definitions;
    private final Map<String, AiToolDefinition> byName;

    public DefaultAiToolCatalog(AiProperties properties, AnalyticsService analyticsService,
                                FinanceiroIndustrialService financeiroService,
                                DesempenhoService desempenhoService,
                                QualidadeDadosService qualidadeService,
                                AuditoriaConsultaService auditoriaService) {
        this.properties = properties;
        this.analyticsService = analyticsService;
        this.financeiroService = financeiroService;
        this.desempenhoService = desempenhoService;
        this.qualidadeService = qualidadeService;
        this.auditoriaService = auditoriaService;
        this.definitions = buildDefinitions();
        this.byName = definitions.stream().collect(java.util.stream.Collectors.toUnmodifiableMap(AiToolDefinition::name, d -> d));
    }

    @Override
    public List<AiToolDefinition> definitions() {
        return definitions;
    }

    @Override
    public AiToolResult execute(AiToolRequest request) {
        var definition = byName.get(request.name());
        if (definition == null || !definition.readOnly()) {
            throw new AiToolException(AssistenteErrorCode.AI_TOOL_REJECTED, "Tool não registrada ou não somente leitura.");
        }
        if (request.callNumber() > properties.getMaxToolCalls()) {
            throw new AiToolException(AssistenteErrorCode.AI_RATE_LIMIT, "Limite de chamadas de tool excedido.");
        }
        authorize(definition, request.securityContext());
        var sectorId = longArg(request.arguments(), "setorId");
        if (sectorId != null && !request.securityContext().canAccessSector(sectorId)) {
            throw new AiToolException(AssistenteErrorCode.AI_PERMISSION_DENIED, "Setor fora do escopo autorizado.");
        }
        return executeService(request, definition);
    }

    private AiToolResult executeService(AiToolRequest request, AiToolDefinition definition) {
        var args = request.arguments();
        var period = period(args, "inicio", "fim");
        var query = analyticsQuery(args, period);
        Object result;
        switch (request.name()) {
            case "producao_resumo" -> result = analyticsService.summary(request.securityContext().empresaId(), query, true);
            case "producao_comparacao" -> {
                var previous = period(args, "inicioAnterior", "fimAnterior");
                result = analyticsService.compare(request.securityContext().empresaId(), query,
                        analyticsQuery(args, previous));
            }
            case "producao_serie_temporal" -> result = analyticsService.timeSeries(request.securityContext().empresaId(), query,
                    enumArg(args, "granularidade", GranularidadeAnalitica.class, GranularidadeAnalitica.DIA));
            case "producao_por_setor" -> result = analyticsService.grouped(request.securityContext().empresaId(), query, Dimension.SETOR, page(args));
            case "producao_por_funcionario" -> result = analyticsService.grouped(request.securityContext().empresaId(), query, Dimension.FUNCIONARIO, page(args));
            case "producao_por_peca" -> result = analyticsService.grouped(request.securityContext().empresaId(), query, Dimension.PECA, page(args));
            case "producao_por_maquina" -> result = analyticsService.grouped(request.securityContext().empresaId(), query, Dimension.MAQUINA, page(args));
            case "funcionario_detalhe" -> result = analyticsService.employeeDetail(request.securityContext().empresaId(), requiredLong(args, "funcionarioId"), query);
            case "paradas_por_motivo" -> result = analyticsService.stopReasons(request.securityContext().empresaId(), query, page(args));
            case "desempenho_resumo", "metas_desempenho" -> result = desempenhoService.summary(request.securityContext().empresaId(), query);
            case "desempenho_por_setor" -> result = desempenhoService.compare(request.securityContext().empresaId(), query, Dimension.SETOR, page(args));
            case "valor_industrial_resumo" -> result = financeiroService.summary(request.securityContext().empresaId(), financeiroQuery(args, period));
            case "valor_industrial_comparacao" -> {
                var previous = period(args, "inicioAnterior", "fimAnterior");
                result = financeiroService.compare(request.securityContext().empresaId(), financeiroQuery(args, period), financeiroQuery(args, previous));
            }
            case "qualidade_resumo" -> result = qualidadeService.resumo(request.securityContext().empresaId(), qualityQuery(args, period));
            case "auditoria_resumo" -> {
                var inicio = period.inicio().atOffset(ZoneOffset.UTC);
                var fim = period.fim().atOffset(ZoneOffset.UTC);
                result = auditoriaService.list(request.securityContext().empresaId(),
                        new AuditoriaFiltro(inicio, fim, null, null, null, null, null, null), page(args), request.authentication());
            }
            default -> throw new AiToolException(AssistenteErrorCode.AI_TOOL_REJECTED, "Operação de tool não suportada.");
        }
        var quality = quality(result);
        var warning = result instanceof ResumoProducaoResponse response && response.quantidadeRegistros() == 0
                ? List.of(new AvisoAssistente("AI_NO_DATA", "Nenhum registro foi encontrado no período.", "INFO")) : List.<AvisoAssistente>of();
        var source = new FonteAssistente(definition.sourceType(), definition.name(), definition.description(), period.inicio(), period.fim());
        return new AiToolResult(definition.name(), Map.of("result", result), List.of(source), period, quality, warning);
    }

    private void authorize(AiToolDefinition definition, AssistenteSecurityContext context) {
        for (var permission : definition.requiredPermissions()) {
            if (!context.hasPermission(permission)) {
                throw new AiToolException(AssistenteErrorCode.AI_PERMISSION_DENIED, "Permissão insuficiente para a tool.");
            }
        }
    }

    private List<AiToolDefinition> buildDefinitions() {
        var analytics = Set.of("ANALYTICS_VISUALIZAR");
        var finance = Set.of("FINANCEIRO_INDUSTRIAL_VISUALIZAR");
        var quality = Set.of("QUALIDADE_DADOS_VISUALIZAR");
        var audit = Set.of("AUDITORIA_VISUALIZAR");
        return List.of(
                definition("producao_resumo", "Resumo de produção por período e filtros.", analytics),
                definition("producao_comparacao", "Comparação de produção entre períodos.", analytics),
                definition("producao_serie_temporal", "Série temporal de produção.", analytics),
                definition("producao_por_setor", "Produção agrupada por setor.", analytics),
                definition("producao_por_funcionario", "Produção agrupada por funcionário.", analytics),
                definition("producao_por_peca", "Produção agrupada por peça.", analytics),
                definition("producao_por_maquina", "Produção agrupada por máquina.", analytics),
                definition("funcionario_detalhe", "Detalhe analítico de funcionário.", analytics),
                definition("paradas_por_motivo", "Paradas agrupadas por motivo.", analytics),
                definition("desempenho_resumo", "Desempenho e metas do período.", analytics),
                definition("metas_desempenho", "Metas e atingimento do período.", analytics),
                definition("desempenho_por_setor", "Desempenho agrupado por setor.", analytics),
                definition("valor_industrial_resumo", "Resumo de valor industrial.", finance),
                definition("valor_industrial_comparacao", "Comparação de valor industrial.", finance),
                definition("qualidade_resumo", "Resumo de qualidade e confiabilidade dos dados.", quality),
                definition("auditoria_resumo", "Consulta resumida de auditoria.", audit));
    }

    private AiToolDefinition definition(String name, String description, Set<String> permissions) {
        return new AiToolDefinition(name, "1", description, Map.of("type", "object"), permissions, true,
                properties.getTimeout(), "ERP_TOOL");
    }

    private AnalyticsQuery analyticsQuery(Map<String, Object> args, PeriodoAssistente period) {
        return new AnalyticsQuery(period.inicio(), period.fim(), longArg(args, "setorId"), longArg(args, "funcionarioId"),
                longArg(args, "pecaId"), longArg(args, "maquinaId"), longArg(args, "turnoId"));
    }

    private com.metalurgica.sistemagestaoindustrial.analytics.FinanceiroQuery financeiroQuery(Map<String, Object> args, PeriodoAssistente period) {
        return new com.metalurgica.sistemagestaoindustrial.analytics.FinanceiroQuery(period.inicio(), period.fim(), longArg(args, "setorId"),
                longArg(args, "funcionarioId"), longArg(args, "pecaId"), longArg(args, "maquinaId"), longArg(args, "turnoId"),
                stringArg(args, "moeda"), enumArg(args, "tipoValor", com.metalurgica.sistemagestaoindustrial.domain.TipoValor.class, null));
    }

    private QualidadeDadosQuery qualityQuery(Map<String, Object> args, PeriodoAssistente period) {
        return new QualidadeDadosQuery(period.inicio(), period.fim(), longArg(args, "setorId"), longArg(args, "funcionarioId"),
                longArg(args, "pecaId"), longArg(args, "maquinaId"), longArg(args, "turnoId"), null, null, null, null, null);
    }

    private PeriodoAssistente period(Map<String, Object> args, String startKey, String endKey) {
        var start = stringArg(args, startKey);
        var end = stringArg(args, endKey);
        if (start == null || end == null) throw new AiToolException(AssistenteErrorCode.AI_INVALID_REQUEST, "Período obrigatório para a tool.");
        try {
            return new PeriodoAssistente(LocalDateTime.parse(start), LocalDateTime.parse(end));
        } catch (RuntimeException exception) {
            throw new AiToolException(AssistenteErrorCode.AI_INVALID_REQUEST, "Período da tool inválido.");
        }
    }

    private QualidadeDadosAssistente quality(Object result) {
        QualidadeDadosResumoResponse response = null;
        if (result instanceof ResumoProducaoResponse production) response = production.qualidadeDados();
        if (result instanceof QualidadeDadosResumoResponse quality) response = quality;
        if (response == null) return null;
        return new QualidadeDadosAssistente(response.status().name(), response.alertas(), response.alertasCriticos(),
                response.coberturaTempoPadrao() == null ? null : response.coberturaTempoPadrao().toString());
    }

    private org.springframework.data.domain.Pageable page(Map<String, Object> args) {
        var value = longArg(args, "limit");
        int limit = value == null ? properties.getMaxToolResults() : Math.toIntExact(Math.min(value, properties.getMaxToolResults()));
        return PageRequest.of(0, Math.max(1, Math.min(limit, 100)));
    }

    private Long requiredLong(Map<String, Object> args, String name) {
        var value = longArg(args, name);
        if (value == null) throw new AiToolException(AssistenteErrorCode.AI_INVALID_REQUEST, "Argumento obrigatório ausente: " + name);
        return value;
    }

    private Long longArg(Map<String, Object> args, String name) {
        var value = args.get(name);
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        try { return Long.valueOf(String.valueOf(value)); }
        catch (NumberFormatException exception) { throw new AiToolException(AssistenteErrorCode.AI_INVALID_REQUEST, "Argumento inválido: " + name); }
    }

    private String stringArg(Map<String, Object> args, String name) {
        var value = args.get(name);
        return value == null ? null : String.valueOf(value);
    }

    private <T extends Enum<T>> T enumArg(Map<String, Object> args, String name, Class<T> type, T fallback) {
        var value = stringArg(args, name);
        if (value == null) return fallback;
        try { return Enum.valueOf(type, value.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException exception) { throw new AiToolException(AssistenteErrorCode.AI_INVALID_REQUEST, "Enum inválido: " + name); }
    }
}
