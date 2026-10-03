package com.metalurgica.sistemagestaoindustrial.resource;

import com.metalurgica.sistemagestaoindustrial.dto.response.*;
import com.metalurgica.sistemagestaoindustrial.report.ReportQueryFactory;
import com.metalurgica.sistemagestaoindustrial.service.RelatoriosOperacionaisService;
import com.metalurgica.sistemagestaoindustrial.service.RelatorioSimplificadoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/relatorios")
public class RelatoriosOperacionaisResource {
    private final RelatoriosOperacionaisService service;
    private final RelatorioSimplificadoService simplified;
    private final ReportQueryFactory queryFactory;

    public RelatoriosOperacionaisResource(RelatoriosOperacionaisService service, RelatorioSimplificadoService simplified, ReportQueryFactory queryFactory) { this.service = service; this.simplified = simplified; this.queryFactory = queryFactory; }

    @GetMapping("/simplificado")
    public RelatorioSimplificadoResponse simplified(@PathVariable Long empresaId, @RequestParam LocalDateTime inicio, @RequestParam LocalDateTime fim,
                                                    @RequestParam(required=false) Long setorId, @RequestParam(required=false) Long funcionarioId,
                                                    @RequestParam(required=false) Long pecaId) {
        return simplified.generate(empresaId, new com.metalurgica.sistemagestaoindustrial.analytics.AnalyticsQuery(inicio, fim, setorId, funcionarioId, pecaId, null, null));
    }

    @GetMapping("/producao")
    public RelatorioProducaoResponse production(@PathVariable Long empresaId, @RequestParam LocalDateTime inicio, @RequestParam LocalDateTime fim,
                                                 @RequestParam(required=false) LocalDateTime inicioAnterior, @RequestParam(required=false) LocalDateTime fimAnterior,
                                                 @RequestParam(required=false) Long setorId, @RequestParam(required=false) Long funcionarioId, @RequestParam(required=false) Long pecaId,
                                                 @RequestParam(required=false) Long maquinaId, @RequestParam(required=false) Long turnoId, @RequestParam(required=false) Integer top) {
        return service.production(empresaId, query(inicio, fim, inicioAnterior, fimAnterior, setorId, funcionarioId, pecaId, maquinaId, turnoId, top));
    }

    @GetMapping("/paradas")
    public RelatorioParadasResponse stops(@PathVariable Long empresaId, @RequestParam LocalDateTime inicio, @RequestParam LocalDateTime fim,
                                          @RequestParam(required=false) Long setorId, @RequestParam(required=false) Integer top) {
        return service.stops(empresaId, query(inicio, fim, null, null, setorId, null, null, null, null, top));
    }

    @GetMapping("/desempenho")
    public RelatorioDesempenhoResponse performance(@PathVariable Long empresaId, @RequestParam LocalDateTime inicio, @RequestParam LocalDateTime fim,
                                                   @RequestParam(required=false) Long setorId, @RequestParam(required=false) Integer top) {
        return service.performance(empresaId, query(inicio, fim, null, null, setorId, null, null, null, null, top));
    }

    private com.metalurgica.sistemagestaoindustrial.report.ReportQuery query(LocalDateTime inicio, LocalDateTime fim, LocalDateTime inicioAnterior, LocalDateTime fimAnterior,
                                                                               Long setorId, Long funcionarioId, Long pecaId, Long maquinaId, Long turnoId, Integer top) {
        return queryFactory.create(inicio, fim, inicioAnterior, fimAnterior, setorId, funcionarioId, pecaId, maquinaId, turnoId, top);
    }
}
