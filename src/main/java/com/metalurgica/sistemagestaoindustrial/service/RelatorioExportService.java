package com.metalurgica.sistemagestaoindustrial.service;

import com.metalurgica.sistemagestaoindustrial.dto.response.*;
import com.metalurgica.sistemagestaoindustrial.report.ReportLimitsProperties;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.metalurgica.sistemagestaoindustrial.observability.OperationalMetrics;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RelatorioExportService {
    private final ReportLimitsProperties limits;
    private final OperationalMetrics metrics;

    public RelatorioExportService(ReportLimitsProperties limits) { this(limits, null); }
    @Autowired
    public RelatorioExportService(ReportLimitsProperties limits, OperationalMetrics metrics) { this.limits = limits; this.metrics = metrics; }

    public byte[] csv(RelatorioExecutivoResponse report) {
        long started = metrics == null ? 0 : metrics.start();
        try {
        validateSize(report);
        var out = new StringBuilder("sep=;\r\n");
        row(out, "Secao", "Campo", "Valor");
        var id = report.identificacao();
        row(out, "Identificacao", "empresaId", id.empresaId());
        row(out, "Identificacao", "empresaNome", id.empresaNome());
        row(out, "Identificacao", "periodoInicio", id.periodoInicio());
        row(out, "Identificacao", "periodoFim", id.periodoFim());
        var p = report.producao();
        row(out, "Producao", "quantidadeProduzida", p.quantidadeProduzida());
        row(out, "Producao", "quantidadeRegistros", p.quantidadeRegistros());
        row(out, "Producao", "tempoParadoMinutos", p.tempoParadoMinutos());
        row(out, "Producao", "tempoProdutivoMinutos", p.tempoProdutivoMinutos());
        for (var item : report.setores()) row(out, "Setores", item.codigo(), item.quantidadeProduzida());
        for (var item : report.paradas()) row(out, "Paradas", item.codigo(), item.tempoParadoMinutos());
        if (report.financeiro() != null) for (var item : report.financeiro()) row(out, "Financeiro", item.moeda(), item.valorTotalCalculado());
        for (var point : report.pontosAtencao()) row(out, "PontosAtencao", "mensagem", point);
        byte[] result = out.toString().getBytes(StandardCharsets.UTF_8);
        if (metrics != null) metrics.report(OperationalMetrics.ReportFormatTag.CSV, OperationalMetrics.Outcome.SUCCESS, started);
        return result;
        } catch (RuntimeException exception) { if (metrics != null) metrics.report(OperationalMetrics.ReportFormatTag.CSV, OperationalMetrics.Outcome.FAILED, started); throw exception; }
    }

    public byte[] xlsx(RelatorioExecutivoResponse report) {
        long started = metrics == null ? 0 : metrics.start();
        try {
            validateSize(report);
            try (Workbook workbook = new XSSFWorkbook(); var bytes = new ByteArrayOutputStream()) {
                summarySheet(workbook, report);
                dimensionSheet(workbook, "Setores", report.setores()); dimensionSheet(workbook, "Funcionarios", report.funcionarios());
                dimensionSheet(workbook, "Pecas", report.pecas()); dimensionSheet(workbook, "Maquinas", report.maquinas()); dimensionSheet(workbook, "Turnos", report.turnos());
                stopSheet(workbook, report.paradas()); metaSheet(workbook, report.metas());
                if (report.financeiro() != null) financialSheet(workbook, report.financeiro());
                workbook.write(bytes); byte[] result = bytes.toByteArray();
                if (metrics != null) metrics.report(OperationalMetrics.ReportFormatTag.XLSX, OperationalMetrics.Outcome.SUCCESS, started);
                return result;
            } catch (IOException e) { throw new IllegalStateException("Nao foi possivel gerar XLSX.", e); }
        } catch (RuntimeException e) {
            if (metrics != null) metrics.report(OperationalMetrics.ReportFormatTag.XLSX, OperationalMetrics.Outcome.FAILED, started);
            throw e;
        }
    }

    public static String sanitizeText(String value) {
        if (value == null || value.isBlank()) return value;
        return "=+-@".indexOf(value.charAt(0)) >= 0 ? "'" + value : value;
    }

    private void summarySheet(Workbook workbook, RelatorioExecutivoResponse report) {
        var sheet = workbook.createSheet("Resumo");
        cell(sheet, 0, 0, "Campo"); cell(sheet, 0, 1, "Valor");
        cell(sheet, 1, 0, "Empresa"); cell(sheet, 1, 1, report.identificacao().empresaNome());
        cell(sheet, 2, 0, "Quantidade produzida"); number(sheet, 2, 1, report.producao().quantidadeProduzida());
        cell(sheet, 3, 0, "Tempo parado (min)"); number(sheet, 3, 1, report.producao().tempoParadoMinutos());
        cell(sheet, 4, 0, "Tempo produtivo (min)"); number(sheet, 4, 1, report.producao().tempoProdutivoMinutos());
        cell(sheet, 5, 0, "Periodo inicio"); cell(sheet, 5, 1, report.identificacao().periodoInicio());
        cell(sheet, 6, 0, "Periodo fim"); cell(sheet, 6, 1, report.identificacao().periodoFim());
    }

    private void dimensionSheet(Workbook workbook, String name, List<ProducaoAgrupadaResponse> values) {
        var sheet = workbook.createSheet(name);
        cell(sheet, 0, 0, "Codigo"); cell(sheet, 0, 1, "Nome"); cell(sheet, 0, 2, "Quantidade"); cell(sheet, 0, 3, "Tempo produtivo");
        for (int i = 0; i < values.size(); i++) { var v = values.get(i); cell(sheet, i + 1, 0, v.codigo()); cell(sheet, i + 1, 1, v.nome()); number(sheet, i + 1, 2, v.quantidadeProduzida()); number(sheet, i + 1, 3, v.tempoProdutivoMinutos()); }
    }

    private void stopSheet(Workbook workbook, List<ParadaPorMotivoResponse> values) {
        var sheet = workbook.createSheet("Paradas");
        cell(sheet, 0, 0, "Codigo"); cell(sheet, 0, 1, "Nome"); cell(sheet, 0, 2, "Ocorrencias"); cell(sheet, 0, 3, "Minutos");
        for (int i = 0; i < values.size(); i++) { var v = values.get(i); cell(sheet, i + 1, 0, v.codigo()); cell(sheet, i + 1, 1, v.nome()); number(sheet, i + 1, 2, v.quantidadeOcorrencias()); number(sheet, i + 1, 3, v.tempoParadoMinutos()); }
    }

    private void financialSheet(Workbook workbook, List<ResumoFinanceiroIndustrialResponse> values) {
        var sheet = workbook.createSheet("Financeiro");
        cell(sheet, 0, 0, "Moeda"); cell(sheet, 0, 1, "Tipo"); cell(sheet, 0, 2, "Valor industrial");
        for (int i = 0; i < values.size(); i++) { var v = values.get(i); cell(sheet, i + 1, 0, v.moeda()); cell(sheet, i + 1, 1, v.tipoValor()); number(sheet, i + 1, 2, v.valorTotalCalculado()); }
    }

    private void metaSheet(Workbook workbook, List<ResumoMetaRelatorioResponse> values) {
        var sheet = workbook.createSheet("Metas");
        cell(sheet, 0, 0, "Nome"); cell(sheet, 0, 1, "Tipo"); cell(sheet, 0, 2, "Meta"); cell(sheet, 0, 3, "Inicio");
        for (int i = 0; i < values.size(); i++) { var v = values.get(i).meta(); cell(sheet, i + 1, 0, v.nome()); cell(sheet, i + 1, 1, v.tipo()); number(sheet, i + 1, 2, v.quantidadeMeta()); cell(sheet, i + 1, 3, v.vigenciaInicio()); }
    }

    private void validateSize(RelatorioExecutivoResponse report) {
        int rows = report.setores().size() + report.funcionarios().size() + report.pecas().size() + report.maquinas().size() + report.turnos().size() + report.paradas().size() + report.metas().size();
        if (report.financeiro() != null) rows += report.financeiro().size();
        if (rows > limits.getMaxRows()) throw new IllegalArgumentException("Exportacao excede o limite de linhas configurado.");
    }

    private void row(StringBuilder out, Object... values) { out.append(java.util.Arrays.stream(values).map(this::csvValue).collect(java.util.stream.Collectors.joining(";"))).append("\r\n"); }
    private String csvValue(Object value) { if (value == null) return ""; var text = sanitizeText(String.valueOf(value)); return "\"" + text.replace("\"", "\"\"") + "\""; }
    private void cell(org.apache.poi.ss.usermodel.Sheet sheet, int row, int column, Object value) { var c = sheet.getRow(row) == null ? sheet.createRow(row).createCell(column) : sheet.getRow(row).createCell(column); if (value == null) c.setBlank(); else if (value instanceof LocalDateTime dateTime) c.setCellValue(dateTime); else c.setCellValue(sanitizeText(String.valueOf(value))); }
    private void number(org.apache.poi.ss.usermodel.Sheet sheet, int row, int column, Number value) { var c = sheet.getRow(row) == null ? sheet.createRow(row).createCell(column) : sheet.getRow(row).createCell(column); if (value == null) c.setBlank(); else c.setCellValue(value.doubleValue()); }
}
