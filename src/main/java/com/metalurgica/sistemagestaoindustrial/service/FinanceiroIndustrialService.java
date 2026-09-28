package com.metalurgica.sistemagestaoindustrial.service;

import com.metalurgica.sistemagestaoindustrial.analytics.*;
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
import java.util.stream.Collectors;

@Service
public class FinanceiroIndustrialService {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private final RegistroProducaoRepository producaoRepository; private final ParadaProducaoRepository paradaRepository;
    private final ValorProducaoRepository valorRepository; private final AnalyticsQueryValidator validator; private final EmpresaService empresaService;
    private final SectorScopeService sectorScopeService;

    public FinanceiroIndustrialService(RegistroProducaoRepository producaoRepository, ParadaProducaoRepository paradaRepository,
                                       ValorProducaoRepository valorRepository, AnalyticsQueryValidator validator, EmpresaService empresaService,
                                       SectorScopeService sectorScopeService) {
        this.producaoRepository = producaoRepository; this.paradaRepository = paradaRepository; this.valorRepository = valorRepository; this.validator = validator; this.empresaService = empresaService;
        this.sectorScopeService = sectorScopeService;
    }

    @Transactional(readOnly = true)
    public List<ResumoFinanceiroIndustrialResponse> summary(Long empresaId, FinanceiroQuery query) { var rows = load(empresaId, query); return aggregates(rows).entrySet().stream().map(e -> summary(query, e.getValue())).toList(); }

    @Transactional(readOnly = true)
    public Page<ValorFinanceiroResponse> grouped(Long empresaId, FinanceiroQuery query, Dimension dimension, Pageable pageable) {
        var groups = new LinkedHashMap<String, FinancialAggregate>();
        for (var row : load(empresaId, query)) { var key = groupKey(row.production, dimension, row.valor); groups.computeIfAbsent(key, ignored -> new FinancialAggregate(labelId(row.production, dimension), labelCode(row.production, dimension), labelName(row.production, dimension), row.valor)).add(row); }
        var values = groups.values().stream().map(FinancialAggregate::response).sorted(Comparator.comparing(ValorFinanceiroResponse::valorTotalCalculado, Comparator.nullsLast(Comparator.reverseOrder()))).toList();
        int size = Math.min(100, Math.max(1, pageable.getPageSize())), from = Math.min((int) pageable.getOffset(), values.size()), to = Math.min(from + size, values.size());
        return new PageImpl<>(values.subList(from, to), PageRequest.of(pageable.getPageNumber(), size, pageable.getSort()), values.size());
    }

    @Transactional(readOnly = true)
    public List<SerieFinanceiraResponse> series(Long empresaId, FinanceiroQuery query, GranularidadeAnalitica granularidade) {
        if (granularidade == null) throw new IllegalArgumentException("Granularidade financeira inválida.");
        var groups = new LinkedHashMap<String, FinancialAggregate>();
        for (var row : load(empresaId, query)) { var key = bucket(row.production.getInicio(), granularidade) + ":" + valueKey(row.valor); groups.computeIfAbsent(key, ignored -> new FinancialAggregate(null, bucket(row.production.getInicio(), granularidade).toString(), bucket(row.production.getInicio(), granularidade).toString(), row.valor)).add(row); }
        return groups.values().stream().map(a -> new SerieFinanceiraResponse(LocalDate.parse(a.codigo), a.moeda, a.tipoValor, a.real, a.value, a.coverage())).toList();
    }

    @Transactional(readOnly = true)
    public List<ComparativoFinanceiroResponse> compare(Long empresaId, FinanceiroQuery atual, FinanceiroQuery anterior) {
        var now = aggregates(load(empresaId, atual)); var before = aggregates(load(empresaId, anterior));
        var keys = new LinkedHashSet<String>(); keys.addAll(now.keySet()); keys.addAll(before.keySet());
        return keys.stream().map(k -> { FinancialAggregate a = now.get(k); FinancialAggregate b = before.get(k); BigDecimal av = a == null ? BigDecimal.ZERO : a.value; BigDecimal bv = b == null ? BigDecimal.ZERO : b.value; BigDecimal absolute = av.subtract(bv).setScale(2, RoundingMode.HALF_UP); BigDecimal pct = FinanceiroMath.variacaoPercentual(av, bv); return new ComparativoFinanceiroResponse(a != null ? a.moeda : b.moeda, a != null ? a.tipoValor : b.tipoValor, av, bv, absolute, pct); }).toList();
    }

    private List<Row> load(Long empresaId, FinanceiroQuery q) {
        empresaService.find(empresaId); validator.validate(empresaId, new AnalyticsQuery(q.inicio(), q.fim(), q.setorId(), q.funcionarioId(), q.pecaId(), q.maquinaId(), q.turnoId()));
        Specification<RegistroProducao> spec = (root, query, cb) -> cb.and(cb.equal(root.get("empresa").get("id"), empresaId), cb.isTrue(root.get("ativo")), cb.greaterThanOrEqualTo(root.get("inicio"), q.inicio()), cb.lessThanOrEqualTo(root.get("inicio"), q.fim()), sectorScopeService.restrict(root, cb, empresaId, "setor"));
        if (q.setorId() != null) spec = spec.and((r,x,cb) -> cb.equal(r.get("setor").get("id"), q.setorId())); if (q.funcionarioId() != null) spec = spec.and((r,x,cb) -> cb.equal(r.get("funcionario").get("id"), q.funcionarioId())); if (q.pecaId() != null) spec = spec.and((r,x,cb) -> cb.equal(r.get("peca").get("id"), q.pecaId())); if (q.maquinaId() != null) spec = spec.and((r,x,cb) -> cb.equal(r.get("maquina").get("id"), q.maquinaId())); if (q.turnoId() != null) spec = spec.and((r,x,cb) -> cb.equal(r.get("turno").get("id"), q.turnoId()));
        var productions = producaoRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "inicio")); if (productions.isEmpty()) return List.of();
        var stopped = paradaRepository.findByRegistroProducaoIdInAndAtivoTrue(productions.stream().map(RegistroProducao::getId).toList()).stream().collect(Collectors.groupingBy(p -> p.getRegistroProducao().getId(), Collectors.summingLong(p -> Duration.between(p.getInicio(), p.getFim()).getSeconds())));
        var scope = sectorScopeService.currentScope(empresaId);
        var values = valorRepository.findActiveForPeriod(empresaId, q.inicio().toLocalDate(), q.fim().toLocalDate()).stream().filter(v -> scope.isEmpty() || scope.get().contains(v.getSetor().getId())).filter(v -> q.tipoEfetivo() == v.getTipoValor()).filter(v -> q.moeda() == null || q.moeda().equalsIgnoreCase(v.getMoeda())).toList();
        var rows = new ArrayList<Row>(); for (var p : productions) { long gross = Duration.between(p.getInicio(), p.getFim()).getSeconds(), productive = Math.max(0, gross - stopped.getOrDefault(p.getId(), 0L)); var applicable = resolve(values, p); if (applicable.isEmpty()) rows.add(new Row(p, gross, productive, null, null)); else for (var v : applicable) rows.add(new Row(p, gross, productive, v, FinanceiroMath.valorProduzido(p.getQuantidade(), v.getValorUnitario()))); } return rows;
    }
    private List<ValorProducao> resolve(List<ValorProducao> values, RegistroProducao p) { var date = p.getInicio().toLocalDate(); return values.stream().filter(v -> v.getPeca().getId().equals(p.getPeca().getId()) && v.getSetor().getId().equals(p.getSetor().getId()) && !v.getVigenciaInicio().isAfter(date) && (v.getVigenciaFim() == null || !v.getVigenciaFim().isBefore(date))).collect(Collectors.groupingBy(v -> v.getMoeda() + ":" + v.getTipoValor())).values().stream().map(list -> list.stream().filter(v -> v.getMaquina() != null && p.getMaquina() != null && v.getMaquina().getId().equals(p.getMaquina().getId())).max(Comparator.comparing(ValorProducao::getVigenciaInicio)).orElseGet(() -> list.stream().filter(v -> v.getMaquina() == null).max(Comparator.comparing(ValorProducao::getVigenciaInicio)).orElse(null))).filter(Objects::nonNull).toList(); }
    private Map<String, FinancialAggregate> aggregates(List<Row> rows) { var result = new LinkedHashMap<String, FinancialAggregate>(); for (var r : rows) if (r.valor != null) result.computeIfAbsent(valueKey(r.valor), ignored -> new FinancialAggregate(null, null, null, r.valor)).add(r); return result; }
    private ResumoFinanceiroIndustrialResponse summary(FinanceiroQuery q, FinancialAggregate a) { return new ResumoFinanceiroIndustrialResponse(q.inicio(), q.fim(), a.moeda, a.tipoValor, a.real, a.value, hourly(a.value, a.gross), hourly(a.value, a.productive), a.coverage()); }
    private BigDecimal hourly(BigDecimal value, long seconds) { return FinanceiroMath.porHora(value, seconds); }
    private String valueKey(ValorProducao v) { return v.getMoeda() + ":" + v.getTipoValor(); }
    private String groupKey(RegistroProducao p, Dimension d, ValorProducao v) { return labelId(p,d) + ":" + labelCode(p,d) + ":" + valueKey(v); }
    private LocalDate bucket(LocalDateTime value, GranularidadeAnalitica g) { return switch(g) { case DIA -> value.toLocalDate(); case MES -> value.toLocalDate().withDayOfMonth(1); case SEMANA -> value.toLocalDate().minusDays(value.getDayOfWeek().getValue() - 1L); }; }
    private Long labelId(RegistroProducao p, Dimension d) { return switch(d) { case SETOR -> p.getSetor().getId(); case FUNCIONARIO -> p.getFuncionario().getId(); case PECA -> p.getPeca().getId(); case MAQUINA -> p.getMaquina() == null ? null : p.getMaquina().getId(); case TURNO -> p.getTurno() == null ? null : p.getTurno().getId(); }; }
    private String labelCode(RegistroProducao p, Dimension d) { return switch(d) { case SETOR -> p.getSetor().getCodigo(); case FUNCIONARIO -> p.getFuncionario().getCodigo(); case PECA -> p.getPeca().getCodigo(); case MAQUINA -> p.getMaquina() == null ? "SEM_MAQUINA" : p.getMaquina().getCodigo(); case TURNO -> p.getTurno() == null ? "SEM_TURNO" : p.getTurno().getCodigo(); }; }
    private String labelName(RegistroProducao p, Dimension d) { return switch(d) { case SETOR -> p.getSetor().getNome(); case FUNCIONARIO -> p.getFuncionario().getNome(); case PECA -> p.getPeca().getDescricao(); case MAQUINA -> p.getMaquina() == null ? "Sem máquina" : p.getMaquina().getNome(); case TURNO -> p.getTurno() == null ? "Sem turno" : p.getTurno().getNome(); }; }

    private static final class Row { final RegistroProducao production; final long gross, productive; final ValorProducao valor; final BigDecimal amount; Row(RegistroProducao p,long g,long pr,ValorProducao v,BigDecimal a){production=p;gross=g;productive=pr;valor=v;amount=a;} }
    private final class FinancialAggregate { final Long id; final String codigo,nome,moeda; final TipoValor tipoValor; BigDecimal real=BigDecimal.ZERO,value=BigDecimal.ZERO,quantityWithValue=BigDecimal.ZERO; long total,withValue,gross,productive; FinancialAggregate(Long i,String c,String n,ValorProducao v){id=i;codigo=c;nome=n;moeda=v==null?null:v.getMoeda();tipoValor=v==null?null:v.getTipoValor();} void add(Row r){real=real.add(r.production.getQuantidade());total++;gross+=r.gross;productive+=r.productive;if(r.valor!=null){withValue++;quantityWithValue=quantityWithValue.add(r.production.getQuantidade());value=value.add(r.amount);}} ValorFinanceiroResponse response(){return new ValorFinanceiroResponse(id,codigo,nome,moeda,tipoValor,real,value,hourly(value,gross),hourly(value,productive),coverage());} CoberturaFinanceiraResponse coverage(){BigDecimal pct=total==0?null:BigDecimal.valueOf(withValue).multiply(HUNDRED).divide(BigDecimal.valueOf(total),2,RoundingMode.HALF_UP);return new CoberturaFinanceiraResponse(total,withValue,total-withValue,quantityWithValue,value,pct);} }
}
