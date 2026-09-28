package com.metalurgica.sistemagestaoindustrial.service;

import com.metalurgica.sistemagestaoindustrial.analytics.AnalyticsQuery;
import com.metalurgica.sistemagestaoindustrial.analytics.Dimension;
import com.metalurgica.sistemagestaoindustrial.domain.*;
import com.metalurgica.sistemagestaoindustrial.dto.response.*;
import com.metalurgica.sistemagestaoindustrial.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.metalurgica.sistemagestaoindustrial.security.SectorScopeService;

import java.math.*;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DesempenhoService {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private final RegistroProducaoRepository producaoRepository; private final ParadaProducaoRepository paradaRepository;
    private final TempoPadraoProducaoRepository tempoRepository; private final MetaProducaoRepository metaRepository;
    private final AnalyticsQueryValidator validator; private final EmpresaService empresaService;
    private final SectorScopeService sectorScopeService;

    public DesempenhoService(RegistroProducaoRepository producaoRepository, ParadaProducaoRepository paradaRepository,
                             TempoPadraoProducaoRepository tempoRepository, MetaProducaoRepository metaRepository,
                             AnalyticsQueryValidator validator, EmpresaService empresaService, SectorScopeService sectorScopeService) {
        this.producaoRepository = producaoRepository; this.paradaRepository = paradaRepository; this.tempoRepository = tempoRepository;
        this.metaRepository = metaRepository; this.validator = validator; this.empresaService = empresaService; this.sectorScopeService = sectorScopeService;
    }

    @Transactional(readOnly = true)
    public DesempenhoResponse summary(Long empresaId, AnalyticsQuery query) {
        var rows = load(empresaId, query); var aggregate = aggregate(rows); var meta = rows.stream().map(r -> r.goal == null ? null : r.goal.getQuantidadeMeta()).filter(Objects::nonNull).findFirst().orElseGet(() -> goal(empresaId, query, null));
        return response(query, aggregate, meta);
    }

    @Transactional(readOnly = true)
    public Page<DesempenhoComparativoResponse> compare(Long empresaId, AnalyticsQuery query, Dimension dimension, Pageable pageable) {
        var rows = load(empresaId, query); var groups = new LinkedHashMap<String, Aggregate>();
        for (var row : rows) { var key = key(row.production, dimension); groups.computeIfAbsent(key, k -> new Aggregate(labelId(row.production, dimension), labelCode(row.production, dimension), labelName(row.production, dimension))).add(row); }
        var goals = metaRepository.findApplicable(empresaId, query.inicio().toLocalDate());
        var values = groups.values().stream().map(a -> { var meta = goalForGroup(a, rows, dimension, goals); return new DesempenhoComparativoResponse(a.id, a.codigo, a.nome, a.real, a.expected, rate(a.real, a.expected), meta, rate(a.real, meta), meta != null, coverage(a)); }).sorted(Comparator.comparing(DesempenhoComparativoResponse::desempenho, Comparator.nullsLast(Comparator.reverseOrder()))).toList();
        int from = Math.min((int) pageable.getOffset(), values.size()), to = Math.min(from + Math.min(100, pageable.getPageSize()), values.size());
        return new PageImpl<>(values.subList(from, to), PageRequest.of(pageable.getPageNumber(), Math.min(100, pageable.getPageSize()), pageable.getSort()), values.size());
    }

    private DesempenhoResponse response(AnalyticsQuery q, Aggregate a, BigDecimal meta) { return new DesempenhoResponse(q.inicio(), q.fim(), a.real, a.expected, rate(a.real, a.expected), meta, rate(a.real, meta), meta != null, coverage(a)); }
    private Aggregate aggregate(List<Row> rows) { var a = new Aggregate(null, null, null); rows.forEach(a::add); return a; }

    private List<Row> load(Long empresaId, AnalyticsQuery query) {
        empresaService.find(empresaId); validator.validate(empresaId, query);
        Specification<RegistroProducao> spec = (root, q, cb) -> cb.and(cb.equal(root.get("empresa").get("id"), empresaId), cb.isTrue(root.get("ativo")), cb.greaterThanOrEqualTo(root.get("inicio"), query.inicio()), cb.lessThanOrEqualTo(root.get("inicio"), query.fim()), sectorScopeService.restrict(root, cb, empresaId, "setor"));
        if (query.setorId() != null) spec = spec.and((r,q,cb) -> cb.equal(r.get("setor").get("id"), query.setorId()));
        if (query.funcionarioId() != null) spec = spec.and((r,q,cb) -> cb.equal(r.get("funcionario").get("id"), query.funcionarioId()));
        if (query.pecaId() != null) spec = spec.and((r,q,cb) -> cb.equal(r.get("peca").get("id"), query.pecaId()));
        if (query.maquinaId() != null) spec = spec.and((r,q,cb) -> cb.equal(r.get("maquina").get("id"), query.maquinaId()));
        if (query.turnoId() != null) spec = spec.and((r,q,cb) -> cb.equal(r.get("turno").get("id"), query.turnoId()));
        var productions = producaoRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "inicio")); if (productions.isEmpty()) return List.of();
        var stopped = paradaRepository.findByRegistroProducaoIdInAndAtivoTrue(productions.stream().map(RegistroProducao::getId).toList()).stream().collect(Collectors.groupingBy(p -> p.getRegistroProducao().getId(), Collectors.summingLong(p -> Duration.between(p.getInicio(), p.getFim()).getSeconds())));
        var scope = sectorScopeService.currentScope(empresaId);
        var standards = tempoRepository.findActiveForPeriod(empresaId, query.inicio().toLocalDate(), query.fim().toLocalDate()).stream().filter(t -> scope.isEmpty() || t.getSetor() == null || scope.get().contains(t.getSetor().getId())).toList();
        var goals = metaRepository.findApplicable(empresaId, query.inicio().toLocalDate()).stream().filter(m -> scope.isEmpty() || m.getSetor() == null || scope.get().contains(m.getSetor().getId())).toList();
        return productions.stream().map(p -> { long gross = Duration.between(p.getInicio(), p.getFim()).getSeconds(); long productive = Math.max(0, gross - stopped.getOrDefault(p.getId(), 0L)); var standard = resolve(standards, p); BigDecimal expected = standard == null ? null : BigDecimal.valueOf(productive).divide(secondsPerUnit(standard), 8, RoundingMode.HALF_UP); return new Row(p, p.getQuantidade(), expected, goalForRecord(goals, p)); }).toList();
    }

    private TempoPadraoProducao resolve(List<TempoPadraoProducao> standards, RegistroProducao p) { return standards.stream().filter(t -> t.getPeca().getId().equals(p.getPeca().getId()) && t.getSetor().getId().equals(p.getSetor().getId()) && (t.getVigenciaFim() == null || !t.getVigenciaFim().isBefore(p.getInicio().toLocalDate())) && !t.getVigenciaInicio().isAfter(p.getInicio().toLocalDate()) && (t.getMaquina() == null || (p.getMaquina() != null && t.getMaquina().getId().equals(p.getMaquina().getId())))).sorted(Comparator.comparing((TempoPadraoProducao t) -> t.getMaquina() == null ? 0 : 1).reversed().thenComparing(TempoPadraoProducao::getVigenciaInicio, Comparator.reverseOrder())).findFirst().orElse(null); }
    private BigDecimal secondsPerUnit(TempoPadraoProducao t) { return t.getBaseTempo() == BaseTempoPadrao.POR_UNIDADE ? t.getSegundos() : t.getSegundos().divide(t.getQuantidadeReferencia(), 8, RoundingMode.HALF_UP); }
    private MetaProducao goalForRecord(List<MetaProducao> goals, RegistroProducao p) { return goals.stream().filter(m -> applies(m, p)).sorted(Comparator.comparingInt(this::specificity).reversed()).findFirst().orElse(null); }
    private boolean applies(MetaProducao m, RegistroProducao p) { if (m.getVigenciaFim() != null && m.getVigenciaFim().isBefore(p.getInicio().toLocalDate())) return false; if (m.getVigenciaInicio().isAfter(p.getInicio().toLocalDate())) return false; return switch (m.getTipo()) { case EMPRESA -> true; case SETOR -> same(m.getSetor(), p.getSetor()); case FUNCIONARIO -> same(m.getFuncionario(), p.getFuncionario()); case PECA -> same(m.getPeca(), p.getPeca()); case MAQUINA -> same(m.getMaquina(), p.getMaquina()); case TURNO -> same(m.getTurno(), p.getTurno()); case COMPOSTA -> (m.getSetor() == null || same(m.getSetor(), p.getSetor())) && (m.getFuncionario() == null || same(m.getFuncionario(), p.getFuncionario())) && (m.getPeca() == null || same(m.getPeca(), p.getPeca())) && (m.getMaquina() == null || same(m.getMaquina(), p.getMaquina())) && (m.getTurno() == null || same(m.getTurno(), p.getTurno())); }; }
    private boolean same(Object a, Object b) { return a != null && b != null && a.equals(b); }
    private int specificity(MetaProducao m) { return switch (m.getTipo()) { case EMPRESA -> 0; case COMPOSTA -> 5; default -> 1; }; }
    private BigDecimal goal(Long e, AnalyticsQuery q, Dimension d) { var goals = metaRepository.findApplicable(e, q.inicio().toLocalDate()); return goals.stream().filter(m -> m.getTipo() == TipoMeta.EMPRESA).map(MetaProducao::getQuantidadeMeta).findFirst().orElse(null); }
    private BigDecimal goalForGroup(Aggregate a, List<Row> rows, Dimension d, List<MetaProducao> goals) { return rows.stream().filter(r -> key(r.production, d).equals(a.key)).map(r -> r.goal == null ? null : r.goal.getQuantidadeMeta()).filter(Objects::nonNull).findFirst().orElseGet(() -> goals.stream().filter(m -> m.getTipo() == TipoMeta.EMPRESA).map(MetaProducao::getQuantidadeMeta).findFirst().orElse(null)); }
    private BigDecimal rate(BigDecimal n, BigDecimal d) { return n == null || d == null || d.signum() == 0 ? null : n.divide(d, 8, RoundingMode.HALF_UP).multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP); }
    private CoberturaDesempenhoResponse coverage(Aggregate a) { long missing = a.total - a.withStandard; BigDecimal pct = a.total == 0 ? null : BigDecimal.valueOf(a.withStandard).multiply(HUNDRED).divide(BigDecimal.valueOf(a.total), 2, RoundingMode.HALF_UP); return new CoberturaDesempenhoResponse(a.total, a.withStandard, missing, a.coveredQuantity, pct); }
    private String key(RegistroProducao p, Dimension d) { return labelId(p,d) + ":" + labelCode(p,d); }
    private Long labelId(RegistroProducao p, Dimension d) { return switch(d) { case SETOR -> p.getSetor().getId(); case FUNCIONARIO -> p.getFuncionario().getId(); case PECA -> p.getPeca().getId(); case MAQUINA -> p.getMaquina() == null ? null : p.getMaquina().getId(); case TURNO -> p.getTurno() == null ? null : p.getTurno().getId(); }; }
    private String labelCode(RegistroProducao p, Dimension d) { return switch(d) { case SETOR -> p.getSetor().getCodigo(); case FUNCIONARIO -> p.getFuncionario().getCodigo(); case PECA -> p.getPeca().getCodigo(); case MAQUINA -> p.getMaquina() == null ? "SEM_MAQUINA" : p.getMaquina().getCodigo(); case TURNO -> p.getTurno() == null ? "SEM_TURNO" : p.getTurno().getCodigo(); }; }
    private String labelName(RegistroProducao p, Dimension d) { return switch(d) { case SETOR -> p.getSetor().getNome(); case FUNCIONARIO -> p.getFuncionario().getNome(); case PECA -> p.getPeca().getDescricao(); case MAQUINA -> p.getMaquina() == null ? "Sem máquina" : p.getMaquina().getNome(); case TURNO -> p.getTurno() == null ? "Sem turno" : p.getTurno().getNome(); }; }

    private static final class Row { final RegistroProducao production; final BigDecimal real, expected; final MetaProducao goal; Row(RegistroProducao p, BigDecimal r, BigDecimal e, MetaProducao g) { production=p; real=r; expected=e; goal=g; } }
    private static final class Aggregate { final Long id; final String codigo, nome; final String key; BigDecimal real=BigDecimal.ZERO, expected=BigDecimal.ZERO, coveredQuantity=BigDecimal.ZERO; long total, withStandard; Aggregate(Long i,String c,String n){id=i;codigo=c;nome=n;key=i+":"+c;} void add(Row r){real=real.add(r.real); total++; if(r.expected!=null){expected=expected.add(r.expected);coveredQuantity=coveredQuantity.add(r.real);withStandard++;}} }
}
