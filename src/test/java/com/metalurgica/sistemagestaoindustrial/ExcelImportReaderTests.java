package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.importacao.ExcelImportReader;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class ExcelImportReaderTests {
    private final ExcelImportReader reader = new ExcelImportReader();

    @Test
    void aceitaFormatoOficialEpreservaZeros() throws Exception {
        var file = workbook(new String[]{"Data", "Código Funcionário", "Código Peça", "Quantidade", "Hora Início", "Hora Fim"},
                new String[]{"29/09/2026", "00147", "PEC-001", "120", "07:00", "08:00"});
        var result = reader.read(file);

        assertEquals(1, result.totalLinhas());
        assertEquals("00147", result.linhas().get(0).codigoFuncionario());
        assertEquals("PEC-001", result.linhas().get(0).codigoPeca());
        assertEquals("120", result.linhas().get(0).quantidade().toPlainString());
    }

    @Test
    void leTemplateOficialComQuatroLinhas() throws Exception {
        var bytes = Files.readAllBytes(Path.of("docs/templates/PRODUCAO_PADRAO.xlsx"));
        var result = reader.read(new MockMultipartFile("file", "PRODUCAO_PADRAO.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bytes));

        assertEquals(3, result.totalLinhas());
        assertEquals(350, result.linhas().stream().mapToInt(row -> row.quantidade().intValueExact()).sum());
        assertEquals("00147", result.linhas().get(0).codigoFuncionario());
        assertEquals("PEC-002", result.linhas().get(1).codigoPeca());
        assertTrue(result.erros().isEmpty());
    }

    @Test
    void ignoraLinhaVaziaEReportaLinhaParcial() throws Exception {
        var file = workbook(new String[]{"funcionario", "peca", "quantidade", "data", "inicio", "fim"},
                new String[]{"00147", "", "120", "27/09/2026", "08:00", "09:00"});
        var result = reader.read(file);

        assertEquals(1, result.totalLinhas());
        assertTrue(result.linhas().isEmpty());
        assertEquals("PECA_AUSENTE", result.erros().get(0).codigo());
    }

    @Test
    void rejeitaFormatoEHeaderAusentes() throws Exception {
        assertEquals("FORMATO_NAO_SUPORTADO", assertThrows(IllegalArgumentException.class,
                () -> reader.read(new MockMultipartFile("file", "dados.csv", "text/csv", "a,b".getBytes()))).getMessage());
        var file = workbook(new String[]{"funcionario", "peca", "quantidade", "data", "inicio"},
                new String[]{"00147", "PEC-001", "120", "27/09/2026", "08:00"});
        assertEquals("CABECALHO_AUSENTE", assertThrows(IllegalArgumentException.class, () -> reader.read(file)).getMessage());
    }

    @Test
    void reportaDataEHorarioInvalidosPorLinha() throws Exception {
        var file = workbook(new String[]{"Data", "Código Funcionário", "Código Peça", "Quantidade", "Hora Início", "Hora Fim"},
                new String[]{"data-invalida", "00147", "PEC-001", "120", "hora", "08:00"});
        var result = reader.read(file);

        assertTrue(result.linhas().isEmpty());
        assertEquals(1, result.erros().size());
        assertEquals("DATA_INVALIDA", result.erros().get(0).codigo());
    }

    @Test
    void rejeitaFormulaEmLinhaDeDados() throws Exception {
        try (var workbook = new XSSFWorkbook(); var output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Producao");
            String[] headers = {"Data", "Código Funcionário", "Código Peça", "Quantidade", "Hora Início", "Hora Fim"};
            var header = sheet.createRow(0);
            var row = sheet.createRow(1);
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);
            row.createCell(0).setCellValue("29/09/2026"); row.createCell(1).setCellValue("00147");
            row.createCell(2).setCellValue("PEC-001"); row.createCell(3).setCellFormula("1+1");
            row.createCell(4).setCellValue("07:00"); row.createCell(5).setCellValue("08:00");
            workbook.write(output);
            var result = reader.read(new MockMultipartFile("file", "formula.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", output.toByteArray()));
            assertEquals("FORMULA_NAO_PERMITIDA", result.erros().get(0).codigo());
            assertTrue(result.linhas().isEmpty());
        }
    }

    @Test
    void rejeitaArquivoZipComEntriesExcessivas() throws Exception {
        var output = new ByteArrayOutputStream();
        try (var zip = new ZipOutputStream(output)) {
            for (int index = 0; index < 513; index++) {
                zip.putNextEntry(new ZipEntry("entry-" + index));
                zip.write(1);
                zip.closeEntry();
            }
        }
        assertEquals("ARQUIVO_CORROMPIDO", assertThrows(IllegalArgumentException.class,
                () -> reader.read(new MockMultipartFile("file", "dados.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", output.toByteArray()))).getMessage());
    }

    private MockMultipartFile workbook(String[] headers, String[] values) throws Exception {
        try (var workbook = new XSSFWorkbook(); var output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Producao");
            var header = sheet.createRow(0);
            var row = sheet.createRow(1);
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
                row.createCell(i).setCellValue(values[i]);
            }
            workbook.write(output);
            return new MockMultipartFile("file", "producao.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", output.toByteArray());
        }
    }
}
