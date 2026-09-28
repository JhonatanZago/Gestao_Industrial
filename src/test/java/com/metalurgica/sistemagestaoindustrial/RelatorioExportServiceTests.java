package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.dto.response.RelatorioExecutivoResponse;
import com.metalurgica.sistemagestaoindustrial.dto.response.ResumoProducaoResponse;
import com.metalurgica.sistemagestaoindustrial.service.RelatorioExportService;
import com.metalurgica.sistemagestaoindustrial.report.ReportLimitsProperties;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import com.metalurgica.sistemagestaoindustrial.dto.response.ProducaoAgrupadaResponse;

import static org.junit.jupiter.api.Assertions.*;

class RelatorioExportServiceTests {
    @Test
    void sanitizesFormulaLikeCsvTextAndKeepsUtf8() {
        assertEquals("'=SUM(A1:A2)", RelatorioExportService.sanitizeText("=SUM(A1:A2)"));
        var report = report("Pintura, acao");
        var csv = new String(new RelatorioExportService(new ReportLimitsProperties()).csv(report), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(csv.contains("Pintura, acao"));
        assertTrue(csv.startsWith("sep=;"));
    }

    @Test
    void createsTypedWorkbookSheetsWithoutFinancialSheetWhenAbsent() throws Exception {
        var bytes = new RelatorioExportService(new ReportLimitsProperties()).xlsx(report("Pintura"));
        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertNotNull(workbook.getSheet("Resumo"));
            assertNotNull(workbook.getSheet("Setores"));
            assertNull(workbook.getSheet("Financeiro"));
            assertEquals(0d, workbook.getSheet("Resumo").getRow(2).getCell(1).getNumericCellValue());
        }
    }

    @Test
    void rejectsExportAboveConfiguredRowLimit() {
        var limits = new ReportLimitsProperties();
        limits.setMaxRows(0);
        assertThrows(IllegalArgumentException.class, () -> new RelatorioExportService(limits).csv(reportWithRows("Pintura")));
    }

    @Test
    void exportsReasonableSyntheticVolumeWithinConfiguredLimit() {
        var limits = new ReportLimitsProperties();
        limits.setMaxRows(1000);
        var bytes = new RelatorioExportService(limits).csv(volumeReport(500));
        assertTrue(bytes.length > 1000);
    }

    private RelatorioExecutivoResponse report(String company) {
        var start = LocalDateTime.of(2026, 9, 1, 0, 0);
        var end = LocalDateTime.of(2026, 9, 30, 23, 59);
        var summary = new ResumoProducaoResponse(start, end, BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null, null, null);
        return new RelatorioExecutivoResponse(new RelatorioExecutivoResponse.Identificacao(1L, company, start, end, OffsetDateTime.now(), "1"), summary, null, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), null, null, List.of(), null);
    }

    private RelatorioExecutivoResponse reportWithRows(String company) {
        var start = LocalDateTime.of(2026, 9, 1, 0, 0);
        var end = LocalDateTime.of(2026, 9, 30, 23, 59);
        var summary = new ResumoProducaoResponse(start, end, BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null, null, null);
        var row = new ProducaoAgrupadaResponse(1L, "PINT", "Pintura", "PINT", "Pintura", BigDecimal.ONE, 1, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE, null, null, null);
        return new RelatorioExecutivoResponse(new RelatorioExecutivoResponse.Identificacao(1L, company, start, end, OffsetDateTime.now(), "1"), summary, null, List.of(row), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), null, null, List.of(), null);
    }

    private RelatorioExecutivoResponse volumeReport(int count) {
        var start = LocalDateTime.of(2026, 9, 1, 0, 0);
        var end = LocalDateTime.of(2026, 9, 30, 23, 59);
        var summary = new ResumoProducaoResponse(start, end, BigDecimal.ZERO, count, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null, null, null);
        var rows = java.util.stream.IntStream.range(0, count).mapToObj(i -> new ProducaoAgrupadaResponse((long) i, "P" + i, "Peca " + i, null, null, BigDecimal.ONE, 1, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE, null, null, null)).toList();
        return new RelatorioExecutivoResponse(new RelatorioExecutivoResponse.Identificacao(1L, "Volume", start, end, OffsetDateTime.now(), "1"), summary, null, rows, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), null, null, List.of(), null);
    }
}
