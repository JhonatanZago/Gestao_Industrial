package com.metalurgica.sistemagestaoindustrial.importacao;

import jakarta.annotation.PostConstruct;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class ExcelImportReader {
    @Value("${app.importacao.max-zip-entry-bytes:20971520}") private long maxZipEntryBytes = 20_971_520L;
    @Value("${app.importacao.max-zip-entries:512}") private long maxZipEntries = 512L;
    @Value("${app.importacao.min-inflate-ratio:0.01}") private double minInflateRatio = 0.01d;

    public ExcelImportReader() { configureZipLimits(); }

    @PostConstruct
    void configureZipLimits() {
        if (maxZipEntryBytes <= 0 || maxZipEntries <= 0 || minInflateRatio <= 0 || minInflateRatio > 1)
            throw new IllegalStateException("Limites ZIP invalidos");
        ZipSecureFile.setMaxEntrySize(maxZipEntryBytes);
        ZipSecureFile.setMaxFileCount(maxZipEntries);
        ZipSecureFile.setMinInflateRatio(minInflateRatio);
    }

    private static final List<String> REQUIRED = List.of("funcionario", "peca", "quantidade", "data", "inicio", "fim");
    private static final Map<String, List<String>> ALIASES = Map.of(
            "funcionario", List.of("funcionario", "codfuncionario", "codigofuncionario", "matricula", "codfunc"),
            "peca", List.of("peca", "codpeca", "codigopeca", "codigodapeca"),
            "quantidade", List.of("quantidade", "qtd", "qtde"),
            "data", List.of("data", "dataproducao", "dia"),
            "inicio", List.of("inicio", "horarioinicio", "horainicio", "horaentrada"),
            "fim", List.of("fim", "horariofim", "horafim", "horasaida"),
            "setor", List.of("setor", "codigosetor"),
            "maquina", List.of("maquina", "codigomaquina", "recurso"),
            "turno", List.of("turno", "codigoturno"));

    public ExcelReadResult read(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("ARQUIVO_VAZIO");
        if (file.getOriginalFilename() == null || !file.getOriginalFilename().toLowerCase(Locale.ROOT).endsWith(".xlsx"))
            throw new IllegalArgumentException("FORMATO_NAO_SUPORTADO");
        try (InputStream in = file.getInputStream(); Workbook workbook = new XSSFWorkbook(in)) {
            DataFormatter formatter = new DataFormatter(Locale.ROOT);
            Sheet sheet = null;
            Map<String, Integer> headers = null;
            for (Sheet candidate : workbook) {
                var found = findHeaders(candidate, formatter);
                if (found != null) { sheet = candidate; headers = found; break; }
            }
            if (sheet == null) throw new IllegalArgumentException("CABECALHO_AUSENTE");

            List<LinhaProducaoImportada> rows = new ArrayList<>();
            List<ImportacaoLinhaErro> errors = new ArrayList<>();
            int headerRow = findHeaderRow(sheet, formatter, headers);
            int last = Math.min(sheet.getLastRowNum(), headerRow + 10000);
            for (int n = headerRow + 1; n <= last; n++) {
                Row row = sheet.getRow(n);
                if (empty(row, formatter)) continue;
                int line = n + 1;
                String func = text(row, headers.get("funcionario"), formatter);
                String peca = text(row, headers.get("peca"), formatter);
                try {
                    if (hasFormula(row)) throw error("arquivo", "FORMULA_NAO_PERMITIDA", "Formulas nao sao permitidas na planilha.");
                    var qty = decimal(row, headers.get("quantidade"), formatter);
                    var date = date(row, headers.get("data"), formatter);
                    var start = time(row, headers.get("inicio"), formatter);
                    var end = time(row, headers.get("fim"), formatter);
                    if (func == null || func.isBlank()) throw error("codigoFuncionario", "FUNCIONARIO_AUSENTE", "Codigo de funcionario obrigatorio.");
                    if (peca == null || peca.isBlank()) throw error("codigoPeca", "PECA_AUSENTE", "Codigo de peca obrigatorio.");
                    if (qty == null || qty.signum() <= 0) throw error("quantidade", "QUANTIDADE_INVALIDA", "Quantidade deve ser maior que zero.");
                    if (date == null) throw error("data", "DATA_INVALIDA", "Data invalida.");
                    if (start == null || end == null) throw error("horario", "HORARIO_INVALIDO", "Horario invalido.");
                    rows.add(new LinhaProducaoImportada(line, func.trim(), peca.trim(), qty, date, start, end,
                            text(row, headers.get("setor"), formatter), text(row, headers.get("maquina"), formatter), text(row, headers.get("turno"), formatter)));
                } catch (ParseError error) {
                    errors.add(new ImportacaoLinhaErro(line, error.field, error.code, error.getMessage(), func, peca));
                }
            }
            return new ExcelReadResult(rows, errors, rows.size() + errors.size());
        } catch (IllegalArgumentException error) {
            throw error;
        } catch (Exception error) {
            throw new IllegalArgumentException("ARQUIVO_CORROMPIDO");
        }
    }

    private Map<String, Integer> findHeaders(Sheet sheet, DataFormatter formatter) {
        for (int rowNumber = 0; rowNumber <= Math.min(sheet.getLastRowNum(), 10); rowNumber++) {
            Row row = sheet.getRow(rowNumber);
            if (row == null) continue;
            Map<String, Integer> map = new HashMap<>();
            for (Cell cell : row) {
                String normalized = norm(formatter.formatCellValue(cell));
                for (var entry : ALIASES.entrySet()) if (entry.getValue().contains(normalized)) map.putIfAbsent(entry.getKey(), cell.getColumnIndex());
            }
            if (map.keySet().containsAll(REQUIRED)) return map;
        }
        return null;
    }

    private int findHeaderRow(Sheet sheet, DataFormatter formatter, Map<String, Integer> headers) {
        for (int rowNumber = 0; rowNumber <= Math.min(sheet.getLastRowNum(), 10); rowNumber++) {
            Row row = sheet.getRow(rowNumber);
            if (row != null && norm(formatter.formatCellValue(row.getCell(headers.get("funcionario")))).length() > 0) return rowNumber;
        }
        return 0;
    }

    private static String norm(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static boolean empty(Row row, DataFormatter formatter) {
        if (row == null) return true;
        for (Cell cell : row) if (!formatter.formatCellValue(cell).isBlank()) return false;
        return true;
    }

    private static boolean hasFormula(Row row) {
        if (row == null) return false;
        for (Cell cell : row) if (cell.getCellType() == CellType.FORMULA) return true;
        return false;
    }

    private static String text(Row row, Integer index, DataFormatter formatter) {
        return index == null || row == null || row.getCell(index) == null ? null : formatter.formatCellValue(row.getCell(index)).trim();
    }

    private static BigDecimal decimal(Row row, int index, DataFormatter formatter) {
        String value = text(row, index, formatter);
        if (value == null || value.isBlank()) return null;
        try {
            String normalized = value.replace(" ", "");
            if (normalized.contains(",") && normalized.contains(".")) normalized = normalized.replace(".", "").replace(',', '.');
            else if (normalized.contains(",")) normalized = normalized.replace(',', '.');
            return new BigDecimal(normalized);
        } catch (Exception error) { throw error("quantidade", "QUANTIDADE_INVALIDA", "Quantidade invalida."); }
    }

    private static LocalDate date(Row row, int index, DataFormatter formatter) {
        Cell cell = row.getCell(index);
        try {
            if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) return cell.getLocalDateTimeCellValue().toLocalDate();
            String value = text(row, index, formatter);
            if (value == null) return null;
            String[] parts = value.contains("/") ? value.split("/") : value.split("-");
            if (parts.length != 3) return null;
            return value.contains("/") ? LocalDate.of(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]), Integer.parseInt(parts[0])) : LocalDate.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        } catch (Exception error) { return null; }
    }

    private static LocalTime time(Row row, int index, DataFormatter formatter) {
        Cell cell = row.getCell(index);
        try {
            if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) return cell.getLocalDateTimeCellValue().toLocalTime();
            String value = text(row, index, formatter);
            if (value == null) return null;
            for (String pattern : List.of("H:mm", "H:mm:ss")) try { return LocalTime.parse(value, DateTimeFormatter.ofPattern(pattern)); } catch (Exception ignored) { }
            return null;
        } catch (Exception error) { return null; }
    }

    private static ParseError error(String field, String code, String message) { return new ParseError(field, code, message); }
    private static class ParseError extends RuntimeException {
        final String field;
        final String code;
        ParseError(String field, String code, String message) { super(message); this.field = field; this.code = code; }
    }
}
