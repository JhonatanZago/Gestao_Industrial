package com.metalurgica.sistemagestaoindustrial.resource;

import com.metalurgica.sistemagestaoindustrial.dto.response.RelatorioExecutivoResponse;
import com.metalurgica.sistemagestaoindustrial.report.ReportQueryFactory;
import com.metalurgica.sistemagestaoindustrial.service.RelatorioExecutivoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/relatorios")
public class RelatorioExecutivoResource {
    private final RelatorioExecutivoService service;
    private final ReportQueryFactory queryFactory;

    public RelatorioExecutivoResource(RelatorioExecutivoService service, ReportQueryFactory queryFactory) {
        this.service = service;
        this.queryFactory = queryFactory;
    }

    @GetMapping("/executivo")
    public RelatorioExecutivoResponse executive(@PathVariable Long empresaId,
                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime inicio,
                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime fim,
                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(required = false) LocalDateTime inicioAnterior,
                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(required = false) LocalDateTime fimAnterior,
                                                @RequestParam(required = false) Long setorId, @RequestParam(required = false) Long funcionarioId,
                                                @RequestParam(required = false) Long pecaId, @RequestParam(required = false) Long maquinaId,
                                                @RequestParam(required = false) Long turnoId, @RequestParam(required = false) Integer top) {
        var query = queryFactory.create(inicio, fim, inicioAnterior, fimAnterior, setorId, funcionarioId, pecaId, maquinaId, turnoId, top);
        return service.generate(empresaId, query);
    }
}
