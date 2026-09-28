package com.metalurgica.sistemagestaoindustrial.service;

import com.metalurgica.sistemagestaoindustrial.domain.*;
import com.metalurgica.sistemagestaoindustrial.dto.response.*;
import com.metalurgica.sistemagestaoindustrial.exception.ResourceNotFoundException;
import com.metalurgica.sistemagestaoindustrial.quality.*;
import com.metalurgica.sistemagestaoindustrial.repository.*;
import com.metalurgica.sistemagestaoindustrial.security.SectorScopeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QualidadeDadosService {
    private static final Logger LOG = LoggerFactory.getLogger(QualidadeDadosService.class);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final RegistroProducaoRepository producaoRepository;
    private final ParadaProducaoRepository paradaRepository;
    private final TempoPadraoProducaoRepository tempoRepository;
    private final ValorProducaoRepository valorRepository;
    private final MetaProducaoRepository metaRepository;
    private final ImportacaoProducaoRepository importacaoRepository;
    private final SetorRepository setorRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PecaRepository pecaRepository;
    private final MaquinaRepository maquinaRepository;
    private final TurnoRepository turnoRepository;
    private final EmpresaService empresaService;
    private final AnalyticsQueryValidator validator;
    private final SectorScopeService sectorScopeService;
    private final AplicabilidadeHistoricaService applicability;
    private final QualidadeDadosProperties properties;

    public QualidadeDadosService(RegistroProducaoRepository producaoRepository,
                                 ParadaProducaoRepository paradaRepository,
                                 TempoPadraoProducaoRepository tempoRepository,
                                 ValorProducaoRepository valorRepository,
                                 MetaProducaoRepository metaRepository,
                                 ImportacaoProducaoRepository importacaoRepository,
                                 SetorRepository setorRepository,
                                 FuncionarioRepository funcionarioRepository,
                                 PecaRepository pecaRepository,
                                 MaquinaRepository maquinaRepository,
                                 TurnoRepository turnoRepository,
                                 EmpresaService empresaService,
                                 AnalyticsQueryValidator validator,
                                 SectorScopeService sectorScopeService,
                                 AplicabilidadeHistoricaService applicability,
                                 QualidadeDadosProperties properties) {
        this.producaoRepository = producaoRepository;
        this.paradaRepository = paradaRepository;
        this.tempoRepository = tempoRepository;
        this.valorRepository = valorRepository;
        this.metaRepository = metaRepository;
        this.importacaoRepository = importacaoRepository;
        this.setorRepository = setorRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.pecaRepository = pecaRepository;
        this.maquinaRepository = maquinaRepository;
        this.turnoRepository = turnoRepository;
        this.empresaService = empresaService;
        this.validator = validator;
        this.sectorScopeService = sectorScopeService;
        this.applicability = applicability;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public QualidadeDadosResumoResponse resumo(Long empresaId, QualidadeDadosQuery query) {
        var snapshot = snapshot(empresaId, query);
        var problems = problems(snapshot, true);
        var standard = coverage(snapshot.productions, snapshot, true);
        var financial = coverage(snapshot.productions, snapshot, false);
        var financialByCurrency = coverageByCurrency(snapshot);
        var critical = problems.stream().filter(p -> p.severidade() == SeveridadeQualidadeDados.CRITICO).count();
        var status = reliability(standard.percentualCobertura(), financial.percentualCobertura(), critical, snapshot.productions.size());
        LOG.debug("qualidade_dados regra=resumo empresaId={} setorId={} registros={} problemas={} duracaoMs={}",
                empresaId, query.setorId(), snapshot.productions.size(), problems.size(), snapshot.elapsedMs);
        return new QualidadeDadosResumoResponse(empresaId, query.setorId(), query.inicio(), query.fim(), standard, financial,
                financialByCurrency, problems.stream().filter(p -> p.severidade() != SeveridadeQualidadeDados.INFO).count(), critical,
                duplicateGroups(snapshot.productions).size(), status);
    }

    @Transactional(readOnly = true)
    public Page<ProblemaQualidadeResponse> problemas(Long empresaId, QualidadeDadosQuery query, Pageable pageable) {
        var page = bounded(pageable);
        var snapshot = snapshot(empresaId, query);
        var values = problems(snapshot, true).stream()
                .filter(p -> query.categoria() == null || query.categoria() == p.categoria())
                .filter(p -> query.severidade() == null || query.severidade() == p.severidade())
                .filter(p -> query.codigo() == null || query.codigo().isBlank() || query.codigo().equalsIgnoreCase(p.codigo()))
                .filter(p -> query.tipoRecurso() == null || query.tipoRecurso() == p.tipoRecurso())
                .filter(p -> query.recursoId() == null || query.recursoId().equals(p.recursoId()))
                .sorted(Comparator.comparing(ProblemaQualidadeDados::dataReferencia, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ProblemaQualidadeDados::codigo))
                .map(this::response)
                .toList();
        return page(values, page);
    }

    @Transactional(readOnly = true)
    public Page<DuplicidadeProducaoResponse> duplicidades(Long empresaId, QualidadeDadosQuery query, Pageable pageable) {
        var page = bounded(pageable);
        var snapshot = snapshot(empresaId, query);
        var groups = duplicateGroups(snapshot.productions);
        if (groups.size() > properties.getMaxDuplicateGroups()) {
            throw new IllegalArgumentException("A consulta excede o limite de grupos de duplicidade.");
        }
        var values = groups.stream().map(this::duplicateResponse).toList();
        return page(values, page);
    }

    @Transactional(readOnly = true)
    public List<ProblemaQualidadeResponse> problemasDoRecurso(Long empresaId, TipoRecursoQualidadeDados tipo,
                                                               Long recursoId, QualidadeDadosQuery query) {
        assertResource(empresaId, tipo, recursoId);
        return problems(snapshot(empresaId, query), true).stream()
                .filter(p -> p.tipoRecurso() == tipo && Objects.equals(p.recursoId(), recursoId))
                .map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public QualidadeImportacaoResponse quality(Long empresaId, Long importacaoId) {
        var importacao = importacaoRepository.findByIdAndEmpresaId(importacaoId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Importação não encontrada nesta empresa."));
        sectorScopeService.assertAllowed(empresaId, importacao.getSetor().getId());
        var productions = producaoRepository.findByEmpresaIdAndImportacaoId(empresaId, importacaoId);
        if (productions.isEmpty()) {
            return new QualidadeImportacaoResponse(importacaoId, importacao.getLinhasImportadas(), 0, 0, 0, 0, StatusConfiabilidadeDados.BAIXA);
        }
        var start = productions.stream().map(RegistroProducao::getInicio).filter(Objects::nonNull).min(LocalDateTime::compareTo).orElse(LocalDateTime.MIN.plusDays(1));
        var end = productions.stream().map(RegistroProducao::getInicio).filter(Objects::nonNull).max(LocalDateTime::compareTo).orElse(start);
        var snapshot = snapshotForProductions(empresaId, new QualidadeDadosQuery(start, end, importacao.getSetor().getId(), null, null, null, null, null, null, null, null, null), productions);
        var problems = problems(snapshot, true);
        var linesWithAlert = problems.stream().filter(p -> p.recursoId() != null && p.tipoRecurso() == TipoRecursoQualidadeDados.REGISTRO_PRODUCAO)
                .map(ProblemaQualidadeDados::recursoId).distinct().count();
        var standardMissing = problems.stream().filter(p -> "QD-PADRAO-001".equals(p.codigo())).map(ProblemaQualidadeDados::recursoId).distinct().count();
        var valueMissing = problems.stream().filter(p -> "QD-VALOR-001".equals(p.codigo())).map(ProblemaQualidadeDados::recursoId).distinct().count();
        var standard = coverage(productions, snapshot, true).percentualCobertura();
        var financial = coverage(productions, snapshot, false).percentualCobertura();
        var critical = problems.stream().filter(p -> p.severidade() == SeveridadeQualidadeDados.CRITICO).count();
        return new QualidadeImportacaoResponse(importacaoId, importacao.getLinhasImportadas(), linesWithAlert,
                duplicateGroups(productions).size(), standardMissing, valueMissing,
                reliability(standard, financial, critical, productions.size()));
    }

    private Snapshot snapshot(Long empresaId, QualidadeDadosQuery query) {
        long started = System.nanoTime();
        validator.validate(empresaId, query.analyticsQuery());
        validatePeriod(query.inicio(), query.fim());
        Specification<RegistroProducao> spec = (root, ignored, cb) -> cb.and(
                cb.equal(root.get("empresa").get("id"), empresaId),
                cb.isTrue(root.get("ativo")),
                cb.greaterThanOrEqualTo(root.get("inicio"), query.inicio()),
                cb.lessThanOrEqualTo(root.get("inicio"), query.fim()),
                sectorScopeService.restrict(root, cb, empresaId, "setor"));
        if (query.setorId() != null) spec = spec.and((root, ignored, cb) -> cb.equal(root.get("setor").get("id"), query.setorId()));
        if (query.funcionarioId() != null) spec = spec.and((root, ignored, cb) -> cb.equal(root.get("funcionario").get("id"), query.funcionarioId()));
        if (query.pecaId() != null) spec = spec.and((root, ignored, cb) -> cb.equal(root.get("peca").get("id"), query.pecaId()));
        if (query.maquinaId() != null) spec = spec.and((root, ignored, cb) -> cb.equal(root.get("maquina").get("id"), query.maquinaId()));
        if (query.turnoId() != null) spec = spec.and((root, ignored, cb) -> cb.equal(root.get("turno").get("id"), query.turnoId()));
        var productions = producaoRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "inicio"));
        if (productions.size() > properties.getMaxRecords()) {
            throw new IllegalArgumentException("A consulta excede o limite de registros analisáveis.");
        }
        return snapshotForProductions(empresaId, query, productions, started);
    }

    private Snapshot snapshotForProductions(Long empresaId, QualidadeDadosQuery query, List<RegistroProducao> productions) {
        return snapshotForProductions(empresaId, query, productions, System.nanoTime());
    }

    private Snapshot snapshotForProductions(Long empresaId, QualidadeDadosQuery query, List<RegistroProducao> productions, long started) {
        var ids = productions.stream().map(RegistroProducao::getId).toList();
        var stops = ids.isEmpty() ? List.<ParadaProducao>of() : paradaRepository.findByRegistroProducaoIdInAndAtivoTrue(ids);
        var scope = sectorScopeService.currentScope(empresaId);
        var standards = tempoRepository.findActiveForPeriod(empresaId, query.inicio().toLocalDate(), query.fim().toLocalDate()).stream()
                .filter(t -> inScope(scope, t.getSetor() == null ? null : t.getSetor().getId())).toList();
        var values = valorRepository.findActiveForPeriod(empresaId, query.inicio().toLocalDate(), query.fim().toLocalDate()).stream()
                .filter(v -> inScope(scope, v.getSetor() == null ? null : v.getSetor().getId())).toList();
        var goals = metaRepository.findActiveForPeriod(empresaId, query.inicio().toLocalDate(), query.fim().toLocalDate()).stream()
                .filter(m -> m.getSetor() == null || inScope(scope, m.getSetor().getId())).toList();
        long elapsedMs = (System.nanoTime() - started) / 1_000_000;
        LOG.debug("qualidade_dados regra=carregamento empresaId={} setorId={} registros={} padroes={} valores={} duracaoMs={}",
                empresaId, query.setorId(), productions.size(), standards.size(), values.size(), elapsedMs);
        return new Snapshot(empresaId, productions, stops, standards, values, goals, elapsedMs);
    }

    private List<ProblemaQualidadeDados> problems(Snapshot snapshot, boolean includeDuplicates) {
        var result = new ArrayList<ProblemaQualidadeDados>();
        var stopsByProduction = snapshot.stops.stream().collect(Collectors.groupingBy(p -> p.getRegistroProducao().getId()));
        for (var production : snapshot.productions) {
            temporalProblems(snapshot, production, result);
            if (production.getQuantidade() == null || production.getQuantidade().signum() <= 0
                    || production.getQuantidade().compareTo(properties.getSuspiciousQuantity()) >= 0) {
                result.add(problem("QD-QTD-001", CategoriaQualidadeDados.PRODUCAO, SeveridadeQualidadeDados.ALERTA,
                        "Quantidade de produção nula, não positiva ou extrema.", snapshot.companyId, TipoRecursoQualidadeDados.REGISTRO_PRODUCAO,
                        production.getId(), sectorId(production), production.getInicio(), details("quantidade", production.getQuantidade())));
            }
            if (production.getSetor() == null || production.getFuncionario() == null || production.getPeca() == null) {
                result.add(problem("QD-CAD-001", CategoriaQualidadeDados.CADASTRO, SeveridadeQualidadeDados.CRITICO,
                        "Registro de produção sem contexto cadastral obrigatório.", snapshot.companyId, TipoRecursoQualidadeDados.REGISTRO_PRODUCAO,
                        production.getId(), sectorId(production), production.getInicio(), Map.of()));
            }
            if (applicability.tempoPadrao(snapshot.standards, production) == null) {
                result.add(problem("QD-PADRAO-001", CategoriaQualidadeDados.COBERTURA, SeveridadeQualidadeDados.ALERTA,
                        "Registro sem tempo padrão aplicável na data e contexto da produção.", snapshot.companyId,
                        TipoRecursoQualidadeDados.REGISTRO_PRODUCAO, production.getId(), sectorId(production), production.getInicio(),
                        details("pecaId", entityId(production.getPeca()), "setorId", sectorId(production))));
            }
            if (applicability.valores(snapshot.values, production).isEmpty()) {
                result.add(problem("QD-VALOR-001", CategoriaQualidadeDados.COBERTURA, SeveridadeQualidadeDados.ALERTA,
                        "Registro sem valor industrial aplicável na data e contexto da produção.", snapshot.companyId,
                        TipoRecursoQualidadeDados.REGISTRO_PRODUCAO, production.getId(), sectorId(production), production.getInicio(),
                        details("pecaId", entityId(production.getPeca()), "setorId", sectorId(production))));
            }
            var productionStops = stopsByProduction.getOrDefault(production.getId(), List.of());
            stopProblems(snapshot, production, productionStops, result);
        }
        validityProblems(snapshot, result);
        if (includeDuplicates) {
            for (var group : duplicateGroups(snapshot.productions)) {
                var first = group.get(0);
                result.add(problem("QD-DUP-001", CategoriaQualidadeDados.DUPLICIDADE, SeveridadeQualidadeDados.ALERTA,
                        "Registros de produção com a mesma chave operacional.", snapshot.companyId,
                        TipoRecursoQualidadeDados.REGISTRO_PRODUCAO, first.getId(), sectorId(first), first.getInicio(),
                        details("registrosRelacionados", group.stream().map(RegistroProducao::getId).toList())));
            }
        }
        return result;
    }

    private void temporalProblems(Snapshot snapshot, RegistroProducao production, List<ProblemaQualidadeDados> result) {
        var start = production.getInicio();
        var end = production.getFim();
        if (start == null || end == null || !end.isAfter(start)) {
            result.add(problem("QD-TEMP-001", CategoriaQualidadeDados.CONSISTENCIA_TEMPORAL, SeveridadeQualidadeDados.CRITICO,
                    "O fim da produção deve ser posterior ao início.", snapshot.companyId, TipoRecursoQualidadeDados.REGISTRO_PRODUCAO,
                    production.getId(), sectorId(production), start, Map.of()));
            return;
        }
        if (end.isAfter(LocalDateTime.now().plus(properties.getFutureTolerance())) || start.isAfter(LocalDateTime.now().plus(properties.getFutureTolerance()))) {
            result.add(problem("QD-TEMP-002", CategoriaQualidadeDados.CONSISTENCIA_TEMPORAL, SeveridadeQualidadeDados.ALERTA,
                    "A produção contém data futura além da tolerância configurada.", snapshot.companyId, TipoRecursoQualidadeDados.REGISTRO_PRODUCAO,
                    production.getId(), sectorId(production), start, details("tolerancia", properties.getFutureTolerance().toString())));
        }
        var duration = Duration.between(start, end);
        if (duration.compareTo(properties.getMaxProductionDuration()) > 0) {
            result.add(problem("QD-TEMP-002", CategoriaQualidadeDados.CONSISTENCIA_TEMPORAL, SeveridadeQualidadeDados.ALERTA,
                    "A duração bruta excede o limite configurado.", snapshot.companyId, TipoRecursoQualidadeDados.REGISTRO_PRODUCAO,
                    production.getId(), sectorId(production), start, details("duracaoSegundos", duration.toSeconds())));
        }
        if (duration.compareTo(properties.getShortProductionDuration()) <= 0 && production.getQuantidade() != null && production.getQuantidade().signum() > 0) {
            var rate = production.getQuantidade().multiply(BigDecimal.valueOf(3600)).divide(BigDecimal.valueOf(Math.max(1, duration.toSeconds())), 4, RoundingMode.HALF_UP);
            if (rate.compareTo(properties.getSuspiciousRatePerHour()) >= 0) {
                result.add(problem("QD-TAXA-001", CategoriaQualidadeDados.PRODUCAO, SeveridadeQualidadeDados.ALERTA,
                        "Taxa de produção suspeita para um intervalo curto.", snapshot.companyId, TipoRecursoQualidadeDados.REGISTRO_PRODUCAO,
                        production.getId(), sectorId(production), start, details("taxaPorHora", rate)));
            }
        }
    }

    private void stopProblems(Snapshot snapshot, RegistroProducao production, List<ParadaProducao> stops, List<ProblemaQualidadeDados> result) {
        long gross = validDuration(production.getInicio(), production.getFim());
        long stopped = 0;
        var ordered = stops.stream().sorted(Comparator.comparing(ParadaProducao::getInicio, Comparator.nullsLast(Comparator.naturalOrder()))).toList();
        ParadaProducao previous = null;
        for (var stop : ordered) {
            if (stop.getInicio() == null || stop.getFim() == null || !stop.getFim().isAfter(stop.getInicio())) {
                result.add(problem("QD-TEMP-001", CategoriaQualidadeDados.CONSISTENCIA_TEMPORAL, SeveridadeQualidadeDados.CRITICO,
                        "A parada possui intervalo inválido.", snapshot.companyId, TipoRecursoQualidadeDados.PARADA, stop.getId(), sectorId(production), stop.getInicio(), Map.of()));
                continue;
            }
            if (production.getInicio() == null || production.getFim() == null
                    || stop.getInicio().isBefore(production.getInicio()) || stop.getFim().isAfter(production.getFim())) {
                result.add(problem("QD-PARADA-001", CategoriaQualidadeDados.PARADA, SeveridadeQualidadeDados.ALERTA,
                        "A parada está fora do intervalo da produção.", snapshot.companyId, TipoRecursoQualidadeDados.PARADA, stop.getId(), sectorId(production), stop.getInicio(), Map.of()));
            }
            if (previous != null && stop.getInicio().isBefore(previous.getFim())) {
                result.add(problem("QD-PARADA-001", CategoriaQualidadeDados.PARADA, SeveridadeQualidadeDados.ALERTA,
                        "Paradas sobrepostas para a mesma produção.", snapshot.companyId, TipoRecursoQualidadeDados.PARADA, stop.getId(), sectorId(production), stop.getInicio(), Map.of()));
            }
            stopped += Duration.between(stop.getInicio(), stop.getFim()).getSeconds();
            previous = stop;
        }
        if (stopped > gross) {
            result.add(problem("QD-PARADA-002", CategoriaQualidadeDados.PARADA, SeveridadeQualidadeDados.CRITICO,
                    "O tempo parado excede a duração bruta da produção.", snapshot.companyId, TipoRecursoQualidadeDados.REGISTRO_PRODUCAO,
                    production.getId(), sectorId(production), production.getInicio(), details("duracaoBrutaSegundos", gross, "tempoParadoSegundos", stopped)));
        }
    }

    private void validityProblems(Snapshot snapshot, List<ProblemaQualidadeDados> result) {
        checkIntervals(snapshot.standards, TempoPadraoProducao::getPeca, TempoPadraoProducao::getSetor, TempoPadraoProducao::getMaquina,
                TempoPadraoProducao::getVigenciaInicio, TempoPadraoProducao::getVigenciaFim,
                TipoRecursoQualidadeDados.TEMPO_PADRAO, snapshot.companyId, result);
        checkIntervals(snapshot.values, ValorProducao::getPeca, ValorProducao::getSetor, ValorProducao::getMaquina,
                ValorProducao::getVigenciaInicio, ValorProducao::getVigenciaFim,
                TipoRecursoQualidadeDados.VALOR_PRODUCAO, snapshot.companyId, result);
        checkIntervals(snapshot.goals, m -> m.getTipo(), m -> m.getSetor(), m -> m.getPeca(), MetaProducao::getVigenciaInicio,
                MetaProducao::getVigenciaFim, TipoRecursoQualidadeDados.META_PRODUCAO, snapshot.companyId, result);
    }

    private <T> void checkIntervals(List<T> values, Function<T, Object> first, Function<T, Object> second,
                                    Function<T, Object> third, Function<T, LocalDate> start, Function<T, LocalDate> end,
                                    TipoRecursoQualidadeDados resource, Long companyId, List<ProblemaQualidadeDados> result) {
        var groups = values.stream().collect(Collectors.groupingBy(v -> String.valueOf(first.apply(v)) + ":" + String.valueOf(second.apply(v)) + ":" + String.valueOf(third.apply(v))));
        for (var group : groups.values()) {
            var ordered = group.stream().sorted(Comparator.comparing(start)).toList();
            T previous = null;
            for (var current : ordered) {
                if (end.apply(current) != null && end.apply(current).isBefore(start.apply(current))) {
                    result.add(intervalProblem("Vigência invertida no cadastro.", resource, idOf(current), companyId, start.apply(current), result));
                }
                if (previous != null && end.apply(previous) != null) {
                    if (!start.apply(current).isAfter(end.apply(previous))) {
                        result.add(intervalProblem("Vigências sobrepostas no cadastro.", resource, idOf(current), companyId, start.apply(current), result));
                    } else if (start.apply(current).isAfter(end.apply(previous).plusDays(1))) {
                        result.add(intervalProblem("Lacuna entre vigências consecutivas.", resource, idOf(current), companyId, start.apply(current), result));
                    }
                }
                previous = current;
            }
        }
    }

    private ProblemaQualidadeDados intervalProblem(String message, TipoRecursoQualidadeDados resource, Long resourceId,
                                                    Long companyId, LocalDate start, List<ProblemaQualidadeDados> ignored) {
        return problem("QD-VIG-001", CategoriaQualidadeDados.CONSISTENCIA_TEMPORAL, SeveridadeQualidadeDados.ALERTA,
                message, companyId, resource, resourceId, null, start.atStartOfDay(), Map.of());
    }

    private List<List<RegistroProducao>> duplicateGroups(List<RegistroProducao> productions) {
        if (properties.getDuplicateTolerance().isZero()) {
            return productions.stream().collect(Collectors.groupingBy(this::exactKey, LinkedHashMap::new, Collectors.toList())).values().stream()
                    .filter(group -> group.size() > 1).toList();
        }
        var candidates = productions.stream().collect(Collectors.groupingBy(this::duplicateBaseKey, LinkedHashMap::new, Collectors.toList()));
        var result = new ArrayList<List<RegistroProducao>>();
        for (var candidate : candidates.values()) {
            var groups = new ArrayList<List<RegistroProducao>>();
            for (var production : candidate.stream().sorted(Comparator.comparing(RegistroProducao::getInicio)).toList()) {
                var target = groups.stream().filter(group -> closeTo(group.get(group.size() - 1), production)).findFirst().orElse(null);
                if (target == null) { target = new ArrayList<>(); groups.add(target); }
                target.add(production);
            }
            result.addAll(groups.stream().filter(group -> group.size() > 1).toList());
        }
        return result;
    }

    private boolean closeTo(RegistroProducao left, RegistroProducao right) {
        return Math.abs(Duration.between(left.getInicio(), right.getInicio()).toMillis()) <= properties.getDuplicateTolerance().toMillis()
                && Math.abs(Duration.between(left.getFim(), right.getFim()).toMillis()) <= properties.getDuplicateTolerance().toMillis();
    }

    private String exactKey(RegistroProducao p) { return duplicateBaseKey(p) + ":" + p.getInicio() + ":" + p.getFim(); }
    private String duplicateBaseKey(RegistroProducao p) {
        return entityId(p.getEmpresa()) + ":" + entityId(p.getFuncionario()) + ":" + entityId(p.getPeca()) + ":" + entityId(p.getSetor()) + ":"
                + entityId(p.getMaquina()) + ":" + entityId(p.getTurno()) + ":" + decimal(p.getQuantidade());
    }

    private DuplicidadeProducaoResponse duplicateResponse(List<RegistroProducao> group) {
        var first = group.get(0);
        return new DuplicidadeProducaoResponse("QD-DUP-001", first.getEmpresa().getId(), sectorId(first), entityId(first.getFuncionario()), entityId(first.getPeca()),
                entityId(first.getMaquina()), entityId(first.getTurno()), first.getInicio(), first.getFim(), first.getQuantidade(), group.stream().map(RegistroProducao::getId).toList());
    }

    private CoberturaDadosResponse coverage(List<RegistroProducao> productions, Snapshot snapshot, boolean standard) {
        long covered = productions.stream().filter(p -> standard ? applicability.tempoPadrao(snapshot.standards, p) != null : !applicability.valores(snapshot.values, p).isEmpty()).count();
        var coveredQuantity = productions.stream().filter(p -> standard ? applicability.tempoPadrao(snapshot.standards, p) != null : !applicability.valores(snapshot.values, p).isEmpty())
                .map(RegistroProducao::getQuantidade).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        var totalQuantity = productions.stream().map(RegistroProducao::getQuantidade).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        var percentage = productions.isEmpty() ? null : BigDecimal.valueOf(covered).multiply(HUNDRED).divide(BigDecimal.valueOf(productions.size()), 2, RoundingMode.HALF_UP);
        return new CoberturaDadosResponse(productions.size(), covered, productions.size() - covered, coveredQuantity,
                totalQuantity.subtract(coveredQuantity), percentage);
    }

    private StatusConfiabilidadeDados reliability(BigDecimal standard, BigDecimal financial, long critical, int total) {
        if (total == 0 || critical > 0) return StatusConfiabilidadeDados.BAIXA;
        if (standard != null && financial != null && standard.compareTo(properties.getCoverageAlertThresholdPercent()) >= 0
                && financial.compareTo(properties.getCoverageAlertThresholdPercent()) >= 0) return StatusConfiabilidadeDados.ALTA;
        if ((standard != null && standard.signum() > 0) || (financial != null && financial.signum() > 0)) return StatusConfiabilidadeDados.PARCIAL;
        return StatusConfiabilidadeDados.BAIXA;
    }

    private Map<String, CoberturaDadosResponse> coverageByCurrency(Snapshot snapshot) {
        var currencies = snapshot.values.stream().map(ValorProducao::getMoeda).filter(Objects::nonNull).collect(Collectors.toCollection(TreeSet::new));
        var result = new LinkedHashMap<String, CoberturaDadosResponse>();
        for (var currency : currencies) {
            var coveredRecords = snapshot.productions.stream().filter(p -> applicability.valores(snapshot.values, p).stream().anyMatch(v -> currency.equalsIgnoreCase(v.getMoeda()))).toList();
            var coveredQuantity = coveredRecords.stream().map(RegistroProducao::getQuantidade).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            var totalQuantity = snapshot.productions.stream().map(RegistroProducao::getQuantidade).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            var percentage = snapshot.productions.isEmpty() ? null : BigDecimal.valueOf(coveredRecords.size()).multiply(HUNDRED)
                    .divide(BigDecimal.valueOf(snapshot.productions.size()), 2, RoundingMode.HALF_UP);
            result.put(currency, new CoberturaDadosResponse(snapshot.productions.size(), coveredRecords.size(), snapshot.productions.size() - coveredRecords.size(),
                    coveredQuantity, totalQuantity.subtract(coveredQuantity), percentage));
        }
        return result;
    }

    private ProblemaQualidadeResponse response(ProblemaQualidadeDados p) {
        return new ProblemaQualidadeResponse(p.codigo(), p.categoria(), p.severidade(), p.mensagem(), p.empresaId(), p.tipoRecurso(), p.recursoId(), p.setorId(), p.dataReferencia(), p.detalhes());
    }

    private ProblemaQualidadeDados problem(String code, CategoriaQualidadeDados category, SeveridadeQualidadeDados severity,
                                           String message, Long companyId, TipoRecursoQualidadeDados resource, Long resourceId,
                                           Long sectorId, LocalDateTime date, Map<String, Object> details) {
        return new ProblemaQualidadeDados(code, category, severity, message, companyId, resource, resourceId, sectorId, date, details);
    }

    private Map<String, Object> details(Object... values) {
        var result = new LinkedHashMap<String, Object>();
        for (int i = 0; i + 1 < values.length; i += 2) if (values[i + 1] != null) result.put(String.valueOf(values[i]), values[i + 1]);
        return result;
    }

    private void assertResource(Long companyId, TipoRecursoQualidadeDados type, Long resourceId) {
        boolean found = switch (type) {
            case EMPRESA -> empresaService.find(companyId).getId().equals(resourceId);
            case SETOR -> setorRepository.findByIdAndEmpresaId(resourceId, companyId).isPresent();
            case FUNCIONARIO -> funcionarioRepository.findByIdAndEmpresaId(resourceId, companyId).isPresent();
            case PECA -> pecaRepository.findByIdAndEmpresaId(resourceId, companyId).isPresent();
            case MAQUINA -> maquinaRepository.findByIdAndEmpresaId(resourceId, companyId).isPresent();
            case TURNO -> turnoRepository.findByIdAndEmpresaId(resourceId, companyId).isPresent();
            case REGISTRO_PRODUCAO -> producaoRepository.findByIdAndEmpresaId(resourceId, companyId).isPresent();
            case PARADA -> paradaRepository.findByIdAndEmpresaId(resourceId, companyId).isPresent();
            case META_PRODUCAO -> metaRepository.findByIdAndEmpresaId(resourceId, companyId).isPresent();
            case TEMPO_PADRAO -> tempoRepository.findByIdAndEmpresaId(resourceId, companyId).isPresent();
            case VALOR_PRODUCAO -> valorRepository.findByIdAndEmpresaId(resourceId, companyId).isPresent();
            case IMPORTACAO -> importacaoRepository.findByIdAndEmpresaId(resourceId, companyId).isPresent();
        };
        if (!found) throw new ResourceNotFoundException("Recurso não encontrado no contexto da Empresa.");
        if (type == TipoRecursoQualidadeDados.SETOR) sectorScopeService.assertAllowed(companyId, resourceId);
    }

    private void validatePeriod(LocalDateTime start, LocalDateTime end) {
        long days = ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate()) + 1;
        if (days > properties.getMaxPeriodDays()) throw new IllegalArgumentException("O período excede o limite configurado para qualidade de dados.");
    }

    private boolean inScope(Optional<Set<Long>> scope, Long sectorId) { return scope.isEmpty() || (sectorId != null && scope.get().contains(sectorId)); }
    private long validDuration(LocalDateTime start, LocalDateTime end) { return start == null || end == null || !end.isAfter(start) ? 0 : Duration.between(start, end).getSeconds(); }
    private Long sectorId(RegistroProducao p) { return p == null || p.getSetor() == null ? null : p.getSetor().getId(); }
    private Long entityId(Object entity) {
        if (entity instanceof Empresa value) return value.getId();
        if (entity instanceof Setor value) return value.getId();
        if (entity instanceof Funcionario value) return value.getId();
        if (entity instanceof Peca value) return value.getId();
        if (entity instanceof Maquina value) return value.getId();
        if (entity instanceof Turno value) return value.getId();
        return null;
    }
    private <T> Long idOf(T value) { try { return (Long) value.getClass().getMethod("getId").invoke(value); } catch (ReflectiveOperationException ignored) { return null; } }
    private String decimal(BigDecimal value) { return value == null ? "null" : value.stripTrailingZeros().toPlainString(); }

    private Pageable bounded(Pageable pageable) {
        if (pageable == null) return PageRequest.of(0, properties.getMaxPageSize());
        if (pageable.getPageSize() > properties.getMaxPageSize()) throw new IllegalArgumentException("O tamanho da página excede o limite configurado.");
        return PageRequest.of(Math.max(0, pageable.getPageNumber()), Math.max(1, pageable.getPageSize()), pageable.getSort());
    }

    private <T> Page<T> page(List<T> values, Pageable pageable) {
        int from = Math.min((int) pageable.getOffset(), values.size());
        int to = Math.min(from + pageable.getPageSize(), values.size());
        return new PageImpl<>(values.subList(from, to), pageable, values.size());
    }

    private record Snapshot(Long companyId, List<RegistroProducao> productions, List<ParadaProducao> stops,
                            List<TempoPadraoProducao> standards, List<ValorProducao> values, List<MetaProducao> goals,
                            long elapsedMs) { }
}
