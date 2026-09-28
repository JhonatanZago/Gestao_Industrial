package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.importacao.ExcelImportReader;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class ExcelImportReaderTests {
    private final ExcelImportReader reader = new ExcelImportReader();

    @Test
    void aceitaAliasesEQuantidadeDecimalComPonto() throws Exception {
        var file = workbook("Matrícula", "Cód. Peça", "Qtd", "Dia", "Hora Entrada", "Hora Saída", "Setor");
        var result = reader.read(file);

        assertEquals(1, result.totalLinhas());
        assertEquals("00147", result.linhas().get(0).codigoFuncionario());
        assertEquals("54871", result.linhas().get(0).codigoPeca());
        assertEquals("350.5", result.linhas().get(0).quantidade().toPlainString());
    }

    @Test
    void ignoraLinhaVaziaEReportaLinhaParcial() throws Exception {
        var file = workbook("funcionario", "peca", "quantidade", "data", "inicio", "fim");
        var result = reader.read(file);

        assertEquals(1, result.totalLinhas());
        assertTrue(result.linhas().isEmpty());
        assertEquals("PECA_AUSENTE", result.erros().get(0).codigo());
    }

    @Test
    void rejeitaFormatoEHeaderAusentes() throws Exception {
        assertEquals("FORMATO_NAO_SUPORTADO", assertThrows(IllegalArgumentException.class,
                () -> reader.read(new MockMultipartFile("file", "dados.csv", "text/csv", "a,b".getBytes()))).getMessage());
        var file = workbook("funcionario", "peca", "quantidade", "data", "inicio");
        assertEquals("CABECALHO_AUSENTE", assertThrows(IllegalArgumentException.class, () -> reader.read(file)).getMessage());
    }

    private MockMultipartFile workbook(String... headers) throws Exception {
        try (var workbook = new XSSFWorkbook(); var output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Producao");
            var header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);
            var row = sheet.createRow(1);
            for (int i = 0; i < headers.length; i++) {
                var value = switch (headers[i]) {
                    case "Matrícula", "funcionario" -> "00147";
                    case "Cód. Peça" -> "54871";
                    case "peca" -> "";
                    case "Qtd" -> "350.5";
                    case "Dia", "data" -> "27/09/2026";
                    case "Hora Entrada", "inicio" -> "08:00";
                    case "Hora Saída", "fim" -> "09:00";
                    case "Setor" -> "PINT";
                    default -> "";
                };
                if (!value.isEmpty()) row.createCell(i).setCellValue(value);
            }
            workbook.write(output);
            return new MockMultipartFile("file", "producao.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", output.toByteArray());
        }
    }
}
