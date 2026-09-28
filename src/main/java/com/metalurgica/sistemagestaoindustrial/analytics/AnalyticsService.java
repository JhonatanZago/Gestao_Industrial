package com.metalurgica.sistemagestaoindustrial.analytics;

import com.metalurgica.sistemagestaoindustrial.domain.RegistroProducao;
import com.metalurgica.sistemagestaoindustrial.dto.response.ProducaoAgrupadaResponse;
import com.metalurgica.sistemagestaoindustrial.dto.response.ComparativoResponse;
import com.metalurgica.sistemagestaoindustrial.dto.response.FuncionarioAnaliticoResponse;
import com.metalurgica.sistemagestaoindustrial.dto.response.ParadaPorMotivoResponse;
import com.metalurgica.sistemagestaoindustrial.dto.response.ResumoProducaoResponse;
import com.metalurgica.sistemagestaoindustrial.dto.response.SerieTemporalResponse;
import com.metalurgica.sistemagestaoindustrial.domain.ParadaProducao;
import com.metalurgica.sistemagestaoindustrial.repository.FuncionarioRepository;
import com.metalurgica.sistemagestaoindustrial.repository.ParadaProducaoRepository;
import com.metalurgica.sistemagestaoindustrial.repository.RegistroProducaoRepository;
import com.metalurgica.sistemagestaoindustrial.service.AnalyticsQueryValidator;
import com.metalurgica.sistemagestaoindustrial.security.SectorScopeService;
import com.metalurgica.sistemagestaoindustrial.quality.QualidadeDadosQuery;
import com.metalurgica.sistemagestaoindustrial.service.QualidadeDadosService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    private static final Logger LOG = LoggerFactory.getLogger(AnalyticsService.class);
    private final RegistroProducaoRepository producaoRepository;
    private final ParadaProducaoRepository paradaRepository;
    private final AnalyticsQueryValidator validator;
    private final FuncionarioRepository funcionarioRepository;
    private final SectorScopeService sectorScopeService;
    private final QualidadeDadosService qualidadeDadosService;

    public AnalyticsService(RegistroProducaoRepository producaoRepository, ParadaProducaoRepository paradaRepository,
                            AnalyticsQueryValidator validator, FuncionarioRepository funcionarioRepository, SectorScopeService sectorScopeService,
                            QualidadeDadosService qualidadeDadosService) {
        this.producaoRepository = producaoRepository;
        this.paradaRepository = paradaRepository;
        this.validator = validator;
        this.funcionarioRepository = funcionarioRepository;
        this.sectorScopeService = sectorScopeService;
        this.qualidadeDadosService = qualidadeDadosService;
    }

    @Transactional(readOnly = true)
    public ResumoProducaoResponse summary(Long empresaId, AnalyticsQuery query) {
        return summary(empresaId, query, false);
    }

    @Transactional(readOnly = true)
    public ResumoProducaoResponse summary(Long empresaId, AnalyticsQuery query, boolean includeQuality) {
        var records = load(empresaId, query);
        var metrics = metrics(records);
        var quality = includeQuality ? qualidadeDadosService.resumo(empresaId,
                new QualidadeDadosQuery(query.inicio(), query.fim(), query.setorId(), query.funcionarioId(), query.pecaId(), query.maquinaId(), query.turnoId(), null, null, null, null, null)) : null;
        return new ResumoProducaoResponse(query.inicio(), query.fim(), metrics.quantidadeProduzida(), metrics.quantidadeRegistros(), metrics.duracaoBrutaMinutos(), metrics.tempoParadoMinutos(), metrics.tempoProdutivoMinutos(), metrics.taxaProducaoBruta(), metrics.taxaProducaoProdutiva(), metrics.percentualTempoParado(), quality);
    }

    @Transactional(readOnly = true)
    public Page<ProducaoAgrupadaResponse> grouped(Long empresaId, AnalyticsQuery query, Dimension dimension, Pageable pageable) {
        var records = load(empresaId, query);
        var groups = new LinkedHashMap<String, AnalyticsAggregate>();
        for (var record : records) {
            var aggregate = AnalyticsAggregate.forRecord(record, dimension);
            var key = (aggregate.response().id() == null ? "null:" : "id:") + aggregate.response().id() + ":" + aggregate.response().codigo();
            var current = groups.computeIfAbsent(key, ignored -> aggregate);
            current.add(record);
        }
        var content = groups.values().stream().map(AnalyticsAggregate::response)
                .sorted(Comparator.comparing(ProducaoAgrupadaResponse::quantidadeProduzida).reversed().thenComparing(r -> Objects.toString(r.codigo(), "")))
                .toList();
        return page(content, pageable);
    }

    @Transactional(readOnly = true)
    public FuncionarioAnaliticoResponse employeeDetail(Long empresaId, Long funcionarioId, AnalyticsQuery query) {
        var employee = funcionarioRepository.findByIdAndEmpresaId(funcionarioId, empresaId)
                .orElseThrow(() -> new com.metalurgica.sistemagestaoindustrial.exception.ResourceNotFoundException("Funcionário não encontrado no contexto da Empresa."));
        var records = load(empresaId, new AnalyticsQuery(query.inicio(), query.fim(), query.setorId(), funcionarioId, query.pecaId(), query.maquinaId(), query.turnoId()));
        var metrics = metrics(records);
        return new FuncionarioAnaliticoResponse(employee.getId(), employee.getCodigo(), employee.getNome(), query.inicio(), query.fim(), metrics.quantidadeProduzida(), metrics.quantidadeRegistros(), metrics.duracaoBrutaMinutos(), metrics.tempoParadoMinutos(), metrics.tempoProdutivoMinutos(), metrics.taxaProducaoBruta(), metrics.taxaProducaoProdutiva(), metrics.percentualTempoParado(), groups(records, Dimension.PECA, 100), groups(records, Dimension.SETOR, 100));
    }

    @Transactional(readOnly = true)
    public Page<ParadaPorMotivoResponse> stopReasons(Long empresaId, AnalyticsQuery query, Pageable pageable) {
        var records = load(empresaId, query);
        if (records.isEmpty()) return new PageImpl<>(List.of(), bounded(pageable), 0);
        var stops = activeStops(records);
        long totalSeconds = stops.stream().mapToLong(p -> Duration.between(p.getInicio(), p.getFim()).getSeconds()).sum();
        var values = stops.stream().collect(Collectors.groupingBy(p -> p.getMotivoParada().getId(), LinkedHashMap::new, Collectors.toList())).values().stream()
                .map(list -> {
                    var reason = list.get(0).getMotivoParada();
                    long seconds = list.stream().mapToLong(p -> Duration.between(p.getInicio(), p.getFim()).getSeconds()).sum();
                    var minutes = AnalyticsMetrics.from(java.math.BigDecimal.ZERO, 0, seconds, 0).duracaoBrutaMinutos();
                    var percent = totalSeconds == 0 ? null : java.math.BigDecimal.valueOf(seconds).multiply(java.math.BigDecimal.valueOf(100)).divide(java.math.BigDecimal.valueOf(totalSeconds), 2, java.math.RoundingMode.HALF_UP);
                    return new ParadaPorMotivoResponse(reason.getId(), reason.getCodigo(), reason.getNome(), reason.getCategoria(), list.size(), minutes, percent);
                }).sorted(Comparator.comparing(ParadaPorMotivoResponse::tempoParadoMinutos).reversed()).toList();
        int from = Math.min((int) pageable.getOffset(), values.size());
        int to = Math.min(from + pageable.getPageSize(), values.size());
        return new PageImpl<>(values.subList(from, to), bounded(pageable), values.size());
    }

    @Transactional(readOnly = true)
    public List<SerieTemporalResponse> timeSeries(Long empresaId, AnalyticsQuery query, GranularidadeAnalitica granularity) {
        if (granularity == null) throw new IllegalArgumentException("Granularidade analítica inválida.");
        var groups = new LinkedHashMap<LocalDate, List<AnalyticsRecord>>();
        for (var record : load(empresaId, query)) groups.computeIfAbsent(bucket(record.producao().getInicio(), granularity), ignored -> new ArrayList<>()).add(record);
        return groups.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(entry -> {
            var m = metrics(entry.getValue());
            return new SerieTemporalResponse(entry.getKey(), m.quantidadeProduzida(), m.quantidadeRegistros(), m.duracaoBrutaMinutos(), m.tempoParadoMinutos(), m.tempoProdutivoMinutos(), m.taxaProducaoBruta(), m.taxaProducaoProdutiva(), m.percentualTempoParado());
        }).toList();
    }

    @Transactional(readOnly = true)
    public ComparativoResponse compare(Long empresaId, AnalyticsQuery current, AnalyticsQuery previous) {
        var actual = metrics(load(empresaId, current));
        var before = metrics(load(empresaId, previous));
        return new ComparativoResponse(current.inicio(), current.fim(), previous.inicio(), previous.fim(), compare(actual.quantidadeProduzida(), before.quantidadeProduzida()), compare(java.math.BigDecimal.valueOf(actual.quantidadeRegistros()), java.math.BigDecimal.valueOf(before.quantidadeRegistros())), compare(actual.duracaoBrutaMinutos(), before.duracaoBrutaMinutos()), compare(actual.tempoParadoMinutos(), before.tempoParadoMinutos()), compare(actual.tempoProdutivoMinutos(), before.tempoProdutivoMinutos()));
    }

    private ComparativoResponse.ComparativoIndicador compare(java.math.BigDecimal actual, java.math.BigDecimal previous) {
        var absolute = actual.subtract(previous).setScale(2, java.math.RoundingMode.HALF_UP);
        var percentage = previous.signum() == 0 ? null : absolute.divide(previous, 8, java.math.RoundingMode.HALF_UP).multiply(java.math.BigDecimal.valueOf(100)).setScale(2, java.math.RoundingMode.HALF_UP);
        return new ComparativoResponse.ComparativoIndicador(actual, previous, absolute, percentage);
    }

    private Page<ProducaoAgrupadaResponse> page(List<ProducaoAgrupadaResponse> values, Pageable pageable) {
        int from = Math.min((int) pageable.getOffset(), values.size());
        int to = Math.min(from + pageable.getPageSize(), values.size());
        return new PageImpl<>(values.subList(from, to), bounded(pageable), values.size());
    }

    private AnalyticsMetrics metrics(List<AnalyticsRecord> records) {
        var quantity = records.stream().map(AnalyticsRecord::quantidade).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        long gross = records.stream().mapToLong(AnalyticsRecord::duracaoBrutaSegundos).sum();
        long stopped = records.stream().mapToLong(AnalyticsRecord::tempoParadoSegundos).sum();
        return AnalyticsMetrics.from(quantity, records.size(), gross, stopped);
    }

    private List<ProducaoAgrupadaResponse> groups(List<AnalyticsRecord> records, Dimension dimension, int limit) {
        var groups = new LinkedHashMap<String, AnalyticsAggregate>();
        for (var record : records) {
            var aggregate = AnalyticsAggregate.forRecord(record, dimension);
            var identity = aggregate.response().id() + ":" + aggregate.response().codigo();
            groups.computeIfAbsent(identity, ignored -> aggregate).add(record);
        }
        return groups.values().stream().map(AnalyticsAggregate::response).sorted(Comparator.comparing(ProducaoAgrupadaResponse::quantidadeProduzida).reversed()).limit(limit).toList();
    }

    private List<ParadaProducao> activeStops(List<AnalyticsRecord> records) {
        return paradaRepository.findByRegistroProducaoIdInAndAtivoTrue(records.stream().map(r -> r.producao().getId()).toList());
    }

    private LocalDate bucket(LocalDateTime value, GranularidadeAnalitica granularity) {
        return switch (granularity) {
            case DIA -> value.toLocalDate();
            case MES -> value.toLocalDate().withDayOfMonth(1);
            case SEMANA -> value.toLocalDate().minusDays(value.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
        };
    }

    private List<AnalyticsRecord> load(Long empresaId, AnalyticsQuery query) {
        long started = System.nanoTime();
        validator.validate(empresaId, query);
        Specification<RegistroProducao> spec = (root, q, cb) -> cb.and(
                cb.equal(root.get("empresa").get("id"), empresaId),
                cb.isTrue(root.get("ativo")),
                cb.greaterThanOrEqualTo(root.get("inicio"), query.inicio()),
                cb.lessThanOrEqualTo(root.get("inicio"), query.fim()),
                sectorScopeService.restrict(root, cb, empresaId, "setor"));
        if (query.setorId() != null) spec = spec.and((root, q, cb) -> cb.equal(root.get("setor").get("id"), query.setorId()));
        if (query.funcionarioId() != null) spec = spec.and((root, q, cb) -> cb.equal(root.get("funcionario").get("id"), query.funcionarioId()));
        if (query.pecaId() != null) spec = spec.and((root, q, cb) -> cb.equal(root.get("peca").get("id"), query.pecaId()));
        if (query.maquinaId() != null) spec = spec.and((root, q, cb) -> cb.equal(root.get("maquina").get("id"), query.maquinaId()));
        if (query.turnoId() != null) spec = spec.and((root, q, cb) -> cb.equal(root.get("turno").get("id"), query.turnoId()));
        var productions = producaoRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "inicio"));
        if (productions.isEmpty()) {
            logIfSlow(empresaId, query, 0, started);
            return List.of();
        }
        var ids = productions.stream().map(RegistroProducao::getId).toList();
        var stoppedByProduction = paradaRepository.findByRegistroProducaoIdInAndAtivoTrue(ids).stream()
                .collect(Collectors.groupingBy(p -> p.getRegistroProducao().getId(), Collectors.summingLong(p -> Duration.between(p.getInicio(), p.getFim()).getSeconds())));
        var result = productions.stream().map(p -> new AnalyticsRecord(p, p.getQuantidade(), Duration.between(p.getInicio(), p.getFim()).getSeconds(), stoppedByProduction.getOrDefault(p.getId(), 0L))).toList();
        logIfSlow(empresaId, query, result.size(), started);
        return result;
    }

    private void logIfSlow(Long empresaId, AnalyticsQuery query, int records, long started) {
        long elapsedMs = (System.nanoTime() - started) / 1_000_000;
        if (elapsedMs >= 500) LOG.warn("consulta analitica lenta empresaId={} setorId={} funcionarioId={} pecaId={} maquinaId={} turnoId={} registros={} duracaoMs={}", empresaId, query.setorId(), query.funcionarioId(), query.pecaId(), query.maquinaId(), query.turnoId(), records, elapsedMs);
    }

    private Pageable bounded(Pageable pageable) {
        int size = Math.min(Math.max(1, pageable.getPageSize()), 100);
        return PageRequest.of(pageable.getPageNumber(), size, pageable.getSort());
    }
}
