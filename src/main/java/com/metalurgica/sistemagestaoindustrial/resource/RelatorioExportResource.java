package com.metalurgica.sistemagestaoindustrial.resource;

import com.metalurgica.sistemagestaoindustrial.report.ReportFormat;
import com.metalurgica.sistemagestaoindustrial.report.ReportQueryFactory;
import com.metalurgica.sistemagestaoindustrial.report.ReportAuditLogger;
import com.metalurgica.sistemagestaoindustrial.service.RelatorioExportService;
import com.metalurgica.sistemagestaoindustrial.service.RelatorioExecutivoService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/relatorios/executivo")
public class RelatorioExportResource {
    private final RelatorioExecutivoService reports;
    private final ReportQueryFactory queries;
    private final RelatorioExportService exports;
    private final ReportAuditLogger auditLogger;

    public RelatorioExportResource(RelatorioExecutivoService reports, ReportQueryFactory queries, RelatorioExportService exports, ReportAuditLogger auditLogger) {
        this.reports = reports; this.queries = queries; this.exports = exports; this.auditLogger = auditLogger;
    }

    @GetMapping("/exportar")
    public ResponseEntity<byte[]> export(@PathVariable Long empresaId, @RequestParam LocalDateTime inicio, @RequestParam LocalDateTime fim,
                                         @RequestParam ReportFormat formato, @RequestParam(required=false) LocalDateTime inicioAnterior,
                                         @RequestParam(required=false) LocalDateTime fimAnterior, @RequestParam(required=false) Long setorId,
                                         @RequestParam(required=false) Long funcionarioId, @RequestParam(required=false) Long pecaId,
                                         @RequestParam(required=false) Long maquinaId, @RequestParam(required=false) Long turnoId,
                                         @RequestParam(required=false) Integer top) {
        var query = queries.create(inicio, fim, inicioAnterior, fimAnterior, setorId, funcionarioId, pecaId, maquinaId, turnoId, top);
        var report = reports.generate(empresaId, query);
        var body = formato == ReportFormat.CSV ? exports.csv(report) : exports.xlsx(report);
        auditLogger.generated(SecurityContextHolder.getContext().getAuthentication(), empresaId, "EXECUTIVO", query.atual(), formato.name());
        var extension = formato == ReportFormat.CSV ? "csv" : "xlsx";
        var contentType = formato == ReportFormat.CSV ? "text/csv; charset=UTF-8" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        var filename = "relatorio-executivo-" + inicio.toLocalDate() + "-a-" + fim.toLocalDate() + "." + extension;
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"").body(body);
    }
}
