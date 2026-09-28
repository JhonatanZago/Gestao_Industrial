package com.metalurgica.sistemagestaoindustrial.resource;

import com.metalurgica.sistemagestaoindustrial.analytics.*;
import com.metalurgica.sistemagestaoindustrial.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/analytics")
public class AnalyticsResource {
    private final AnalyticsService service;

    public AnalyticsResource(AnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/resumo")
    public ResumoProducaoResponse summary(@PathVariable Long empresaId, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicio, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fim, Filters filters, @RequestParam(defaultValue = "false") boolean incluirQualidade) {
        return service.summary(empresaId, filters.query(inicio, fim), incluirQualidade);
    }

    @GetMapping("/setores")
    public Page<ProducaoAgrupadaResponse> sectors(@PathVariable Long empresaId, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicio, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fim, Filters filters, @PageableDefault(size = 20) Pageable pageable) {
        return service.grouped(empresaId, filters.query(inicio, fim), Dimension.SETOR, pageable);
    }

    @GetMapping("/funcionarios")
    public Page<ProducaoAgrupadaResponse> employees(@PathVariable Long empresaId, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicio, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fim, Filters filters, @PageableDefault(size = 20) Pageable pageable) {
        return service.grouped(empresaId, filters.query(inicio, fim), Dimension.FUNCIONARIO, pageable);
    }

    @GetMapping("/funcionarios/{funcionarioId}")
    public FuncionarioAnaliticoResponse employee(@PathVariable Long empresaId, @PathVariable Long funcionarioId, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicio, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fim, Filters filters) {
        return service.employeeDetail(empresaId, funcionarioId, filters.query(inicio, fim));
    }

    @GetMapping("/pecas")
    public Page<ProducaoAgrupadaResponse> parts(@PathVariable Long empresaId, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicio, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fim, Filters filters, @PageableDefault(size = 20) Pageable pageable) {
        return service.grouped(empresaId, filters.query(inicio, fim), Dimension.PECA, pageable);
    }

    @GetMapping("/maquinas")
    public Page<ProducaoAgrupadaResponse> machines(@PathVariable Long empresaId, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicio, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fim, Filters filters, @PageableDefault(size = 20) Pageable pageable) {
        return service.grouped(empresaId, filters.query(inicio, fim), Dimension.MAQUINA, pageable);
    }

    @GetMapping("/turnos")
    public Page<ProducaoAgrupadaResponse> shifts(@PathVariable Long empresaId, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicio, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fim, Filters filters, @PageableDefault(size = 20) Pageable pageable) {
        return service.grouped(empresaId, filters.query(inicio, fim), Dimension.TURNO, pageable);
    }

    @GetMapping("/paradas/motivos")
    public Page<ParadaPorMotivoResponse> stopReasons(@PathVariable Long empresaId, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicio, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fim, Filters filters, @PageableDefault(size = 20) Pageable pageable) {
        return service.stopReasons(empresaId, filters.query(inicio, fim), pageable);
    }

    @GetMapping("/serie-temporal")
    public List<SerieTemporalResponse> timeSeries(@PathVariable Long empresaId, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicio, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fim, @RequestParam GranularidadeAnalitica granularidade, Filters filters) {
        return service.timeSeries(empresaId, filters.query(inicio, fim), granularidade);
    }

    @GetMapping("/comparativo")
    public ComparativoResponse compare(@PathVariable Long empresaId, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicioAtual, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fimAtual, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicioAnterior, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fimAnterior, Filters filters) {
        return service.compare(empresaId, filters.query(inicioAtual, fimAtual), filters.query(inicioAnterior, fimAnterior));
    }

    public record Filters(Long setorId, Long funcionarioId, Long pecaId, Long maquinaId, Long turnoId) {
        public AnalyticsQuery query(LocalDateTime inicio, LocalDateTime fim) {
            return new AnalyticsQuery(inicio, fim, setorId, funcionarioId, pecaId, maquinaId, turnoId);
        }
    }
}
