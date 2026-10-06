package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.audit.*;
import com.metalurgica.sistemagestaoindustrial.domain.OrigemImportacao;
import com.metalurgica.sistemagestaoindustrial.importacao.AutomaticImportProperties;
import com.metalurgica.sistemagestaoindustrial.importacao.FileStabilityService;
import com.metalurgica.sistemagestaoindustrial.importacao.FileProcessingCoordinator;
import com.metalurgica.sistemagestaoindustrial.importacao.ReconciliationResult;
import com.metalurgica.sistemagestaoindustrial.importacao.ReconciliationService;
import com.metalurgica.sistemagestaoindustrial.importacao.ServidorExcelWatcher;
import com.metalurgica.sistemagestaoindustrial.service.ImportacaoProducaoService;
import com.metalurgica.sistemagestaoindustrial.repository.*;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:importacaoexcel;MODE=PostgreSQL;DB_CLOSE_DELAY=0;DATABASE_TO_LOWER=TRUE")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImportacaoExcelIntegrationTests extends AuthenticatedIntegrationTest {
    @TempDir Path tempDirectory;
    @Autowired MeterRegistry registry;
    @Autowired EmpresaRepository empresaRepository;
    @Autowired SetorRepository setorRepository;
    @Autowired FuncionarioRepository funcionarioRepository;
    @Autowired PecaRepository pecaRepository;
    @Autowired RegistroProducaoRepository producaoRepository;
    @Autowired ImportacaoProducaoRepository importacaoRepository;
    @Autowired ResultadoLinhaImportacaoRepository resultadoRepository;
    @Autowired ParadaProducaoRepository paradaRepository;
    @Autowired MotivoParadaRepository motivoRepository;
    @Autowired MaquinaRepository maquinaRepository;
    @Autowired TurnoRepository turnoRepository;
    @Autowired EventoAuditoriaRepository eventoAuditoriaRepository;
    @Autowired ImportacaoProducaoService importacaoService;
    @Autowired FileStabilityService fileStabilityService;
    @Autowired FileProcessingCoordinator fileProcessingCoordinator;

    @BeforeEach
    void clean() {
        eventoAuditoriaRepository.deleteAll(); resultadoRepository.deleteAll(); paradaRepository.deleteAll();
        motivoRepository.deleteAll(); producaoRepository.deleteAll(); importacaoRepository.deleteAll();
        maquinaRepository.deleteAll(); turnoRepository.deleteAll(); pecaRepository.deleteAll();
        funcionarioRepository.deleteAll(); setorRepository.deleteAll(); usuarioRepository.deleteAll(); empresaRepository.deleteAll();
    }

    @AfterEach
    void cleanAfter() {
        clean();
    }

    @Test
    void importsValidWorkbookAndPreventsSameHash() throws Exception {
        var f = fixture();
        var file = workbook("00147", "54871", "350", "08:00", "09:00");
        double before = counter("imports.completed", "outcome", "success");
        mvc.perform(multipart(path(f) + "/importacoes/producao").file(file))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.origem").value("MANUAL"))
                .andExpect(jsonPath("$.linhasImportadas").value(1));
        Assertions.assertEquals(before + 1, counter("imports.completed", "outcome", "success"));
        Assertions.assertEquals(1, producaoRepository.count());
        Assertions.assertEquals("IMPORTACAO", producaoRepository.findAll().get(0).getOrigem().name());
        var importEvent = eventoAuditoriaRepository.findAll().stream().filter(e -> e.getTipoRecurso() == TipoRecursoAuditoria.IMPORTACAO).findFirst().orElseThrow();
        Assertions.assertEquals(OrigemAuditoria.IMPORTACAO_EXCEL, importEvent.getOrigem());
        var productionEvent = eventoAuditoriaRepository.findAll().stream().filter(e -> e.getTipoRecurso() == TipoRecursoAuditoria.REGISTRO_PRODUCAO).findFirst().orElseThrow();
        Assertions.assertEquals(importEvent.getCorrelationId(), productionEvent.getCorrelationId());
        mvc.perform(get("/api/v1/empresas/" + f.empresa() + "/analytics/resumo?inicio=2026-09-26T00:00:00&fim=2026-09-26T23:59:59"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantidadeProduzida").value(350));
        mvc.perform(multipart(path(f) + "/importacoes/producao").file(file)).andExpect(status().isConflict());
    }

    @Test
    void watcherImportsTemporaryDirectoryThroughExistingPipeline() throws Exception {
        var f = fixture();
        var properties = new AutomaticImportProperties();
        properties.setEnabled(true); properties.setDebounceMs(10);
        properties.getStability().setIntervalMs(10); properties.getStability().setStableChecks(2); properties.getStability().setTimeoutSeconds(2);
        var directory = new AutomaticImportProperties.Directory();
        directory.setEmpresaId(f.empresa()); directory.setSetorId(f.setor()); directory.setPath(tempDirectory.toString());
        properties.setDirectories(List.of(directory));
        var watcher = new ServidorExcelWatcher(properties, fileStabilityService, importacaoService);
        try {
            watcher.start();
            var workbook = workbook("00147", "54871", "350", "08:00", "09:00");
            Files.write(tempDirectory.resolve("KTL.xlsx"), workbook.getBytes());
            awaitCondition(() -> producaoRepository.count() == 1);
            var history = importacaoRepository.findAll().stream().filter(i -> i.getOrigem() == OrigemImportacao.FILE_WATCHER).findFirst().orElseThrow();
            Assertions.assertEquals("FILE_WATCHER", history.getOrigem().name());
            Assertions.assertEquals("FILE_WATCHER", producaoRepository.findAll().get(0).getOrigem().name());
        } finally { watcher.stop(); }
    }

    @Test
    void reconciliationProcessesMissedVersionAndIgnoresCompletedVersion() throws Exception {
        var f = fixture();
        addEmployee(f.empresa(), "00152", "Operador 2");
        addEmployee(f.empresa(), "00160", "Operador 3");
        addPart(f.empresa(), "PEC-001", "Peca 1");
        addPart(f.empresa(), "PEC-002", "Peca 2");
        addPart(f.empresa(), "PEC-003", "Peca 3");
        var properties = reconciliationProperties(f);
        var service = new ReconciliationService(properties, fileProcessingCoordinator);
        Files.write(tempDirectory.resolve("KTL.xlsx"), workbookRows(new String[][]{
                {"00147", "PEC-001", "100", "07:00", "08:00"},
                {"00152", "PEC-002", "200", "08:00", "09:00"}
        }).getBytes());
        ReconciliationResult first = service.reconciliar();
        Assertions.assertEquals(1, first.arquivosProcessados());
        Assertions.assertEquals(2, producaoRepository.count());
        Assertions.assertEquals(OrigemImportacao.RECONCILIACAO, importacaoRepository.findAll().get(0).getOrigem());

        ReconciliationResult second = service.reconciliar();
        Assertions.assertEquals(1, second.arquivosIgnorados());
        Assertions.assertEquals(2, producaoRepository.count());

        Files.write(tempDirectory.resolve("KTL.xlsx"), workbookRows(new String[][]{
                {"00147", "PEC-001", "100", "07:00", "08:00"},
                {"00152", "PEC-002", "200", "08:00", "09:00"},
                {"00160", "PEC-003", "300", "09:00", "10:00"}
        }).getBytes());
        ReconciliationResult third = service.reconciliar();
        Assertions.assertEquals(1, third.arquivosProcessados());
        Assertions.assertEquals(3, producaoRepository.count());
    }

    private AutomaticImportProperties reconciliationProperties(Fixture f) {
        var properties = new AutomaticImportProperties();
        properties.setEnabled(true); properties.getReconciliation().setEnabled(true);
        properties.getStability().setIntervalMs(10); properties.getStability().setStableChecks(2); properties.getStability().setTimeoutSeconds(2);
        var directory = new AutomaticImportProperties.Directory();
        directory.setEmpresaId(f.empresa()); directory.setSetorId(f.setor()); directory.setPath(tempDirectory.toString());
        properties.setDirectories(List.of(directory));
        return properties;
    }

    @Test
    void importsOnlyNewRowsWhenImportingVersionWithOldAndNewRows() throws Exception {
        var f = fixture();
        addEmployee(f.empresa(), "00152", "Operador 2");
        addEmployee(f.empresa(), "00160", "Operador 3");
        addPart(f.empresa(), "PEC-001", "Peca 1");
        addPart(f.empresa(), "PEC-002", "Peca 2");
        addPart(f.empresa(), "PEC-003", "Peca 3");

        var version1 = workbookRows(new String[][]{
                {"00147", "PEC-001", "100", "07:00", "08:00"},
                {"00152", "PEC-002", "200", "08:00", "09:00"}
        });
        mvc.perform(multipart(path(f) + "/importacoes/producao").file(version1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.linhasImportadas").value(2));
        var version1Hash = importacaoRepository.findAll().get(0).getHashArquivo();

        var version2 = workbookRows(new String[][]{
                {"00147", "PEC-001", "100", "07:00", "08:00"},
                {"00152", "PEC-002", "200", "08:00", "09:00"},
                {"00160", "PEC-003", "300", "09:00", "10:00"}
        });
        mvc.perform(multipart(path(f) + "/importacoes/producao").file(version2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.linhasImportadas").value(1))
                .andExpect(jsonPath("$.linhasIgnoradas").value(2))
                .andExpect(jsonPath("$.linhasInvalidas").value(0));

        Assertions.assertEquals(3, producaoRepository.count());
        var version2Hash = importacaoRepository.findAll().stream()
                .filter(importacao -> !importacao.getHashArquivo().equals(version1Hash))
                .findFirst().orElseThrow().getHashArquivo();
        Assertions.assertNotEquals(version1Hash, version2Hash, "A versão alterada deve gerar hash diferente");
        Assertions.assertEquals(2, importacaoRepository.findAll().stream()
                .filter(importacao -> importacao.getStatus().name().equals("CONCLUIDA")).count());
        Assertions.assertEquals(2, importacaoRepository.count());
        Assertions.assertEquals(2, resultadoRepository.findAll().stream()
                .filter(resultado -> resultado.getStatus().equals("IGNORADO")).count());
    }

    @Test
    void ignoresReorderedRowsAndTreatsChangedContentAsConflict() throws Exception {
        var f = fixture();
        addEmployee(f.empresa(), "00152", "Operador 2");
        addEmployee(f.empresa(), "00160", "Operador 3");
        addPart(f.empresa(), "PEC-001", "Peca 1");
        addPart(f.empresa(), "PEC-002", "Peca 2");
        addPart(f.empresa(), "PEC-003", "Peca 3");
        var v1 = workbookRows(new String[][]{{"00147", "PEC-001", "100", "07:00", "08:00"}, {"00152", "PEC-002", "200", "08:00", "09:00"}});
        mvc.perform(multipart(path(f) + "/importacoes/producao").file(v1)).andExpect(status().isCreated());
        var reordered = workbookRows(new String[][]{{"00147", "PEC-001", "100", "07:00", "08:00"}, {"00160", "PEC-003", "300", "09:00", "10:00"}, {"00152", "PEC-002", "200", "08:00", "09:00"}});
        mvc.perform(multipart(path(f) + "/importacoes/producao").file(reordered)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.linhasImportadas").value(1)).andExpect(jsonPath("$.linhasIgnoradas").value(2));
        Assertions.assertEquals(3, producaoRepository.count());
        var changed = workbookRows(new String[][]{{"00147", "PEC-001", "150", "07:00", "08:00"}});
        mvc.perform(multipart(path(f) + "/importacoes/producao").file(changed)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REJEITADA"));
        Assertions.assertEquals(3, producaoRepository.count());
    }

    @Test
    void rejectsDuplicateRowsInsideTheSameWorkbook() throws Exception {
        var f = fixture();
        var duplicate = workbookRows(new String[][]{
                {"00147", "54871", "100", "07:00", "08:00"},
                {"00147", "54871", "100", "07:00", "08:00"}
        });
        mvc.perform(multipart(path(f) + "/importacoes/producao").file(duplicate))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("REJEITADA"))
                .andExpect(jsonPath("$.linhasInvalidas").value(1));
        Assertions.assertEquals(0, producaoRepository.count());
    }

    @Test
    void growsIncrementallyAcrossThreeVersionsWithoutReimportingHistory() throws Exception {
        var f = fixture();
        addEmployee(f.empresa(), "00152", "Operador 2");
        addEmployee(f.empresa(), "00160", "Operador 3");
        addEmployee(f.empresa(), "00161", "Operador 4");
        addEmployee(f.empresa(), "00162", "Operador 5");
        addPart(f.empresa(), "PEC-001", "Peca 1");
        addPart(f.empresa(), "PEC-002", "Peca 2");
        addPart(f.empresa(), "PEC-003", "Peca 3");
        addPart(f.empresa(), "PEC-004", "Peca 4");
        addPart(f.empresa(), "PEC-005", "Peca 5");
        var a = new String[]{"00147", "PEC-001", "100", "07:00", "08:00"};
        var b = new String[]{"00152", "PEC-002", "200", "08:00", "09:00"};
        var c = new String[]{"00160", "PEC-003", "300", "09:00", "10:00"};
        var d = new String[]{"00161", "PEC-004", "400", "10:00", "11:00"};
        var e = new String[]{"00162", "PEC-005", "500", "11:00", "12:00"};
        mvc.perform(multipart(path(f) + "/importacoes/producao").file(workbookRows(new String[][]{a, b})))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.linhasImportadas").value(2));
        mvc.perform(multipart(path(f) + "/importacoes/producao").file(workbookRows(new String[][]{a, b, c})))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.linhasImportadas").value(1))
                .andExpect(jsonPath("$.linhasIgnoradas").value(2));
        mvc.perform(multipart(path(f) + "/importacoes/producao").file(workbookRows(new String[][]{a, b, c, d, e})))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.linhasImportadas").value(2))
                .andExpect(jsonPath("$.linhasIgnoradas").value(3));
        Assertions.assertEquals(5, producaoRepository.count());
    }

    @Test
    void rejectsFractionalQuantityAndNonIncreasingTime() throws Exception {
        var f = fixture();
        mvc.perform(multipart(path(f) + "/importacoes/producao/validar").file(workbook("00147", "54871", "10.5", "08:00", "09:00")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REJEITADA"))
                .andExpect(jsonPath("$.linhasInvalidas").value(1));
        mvc.perform(multipart(path(f) + "/importacoes/producao/validar").file(workbook("00147", "54871", "10", "09:00", "09:00")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REJEITADA"))
                .andExpect(jsonPath("$.linhasInvalidas").value(1));
        Assertions.assertEquals(0, producaoRepository.count());
    }

    @Test
    void validatesInvalidWorkbookWithoutPersistingProduction() throws Exception {
        var f = fixture();
        var file = workbook("00991", "54871", "350", "08:00", "09:00");
        mvc.perform(multipart(path(f) + "/importacoes/producao/validar").file(file))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REJEITADA"))
                .andExpect(jsonPath("$.linhasInvalidas").value(1));
        Assertions.assertEquals(0, producaoRepository.count());
        var id = importacaoRepository.findAll().get(0).getId();
        mvc.perform(get(path(f) + "/importacoes/producao/" + id + "/linhas"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].codigoErro").value("FUNCIONARIO_NAO_ENCONTRADO"));
    }

    @Test
    void preservesLeadingZerosAndParsesAliases() throws Exception {
        var f = fixture();
        mvc.perform(multipart(path(f) + "/importacoes/producao/validar").file(workbook("00147", "54871", "1", "08:00", "09:00")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.linhasValidas").value(1));
    }

    @Test
    void rejectsUnreadableWorkbookAndTerminatesBatch() throws Exception {
        var f = fixture();
        double before = counter("imports.failed", "outcome", "rejected");
        var broken = new MockMultipartFile("file", "broken.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[]{1, 2, 3});
        mvc.perform(multipart(path(f) + "/importacoes/producao/validar").file(broken).header("X-Correlation-Id", "read-failure-1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REJEITADA"))
                .andExpect(jsonPath("$.erroCodigo").value("ARQUIVO_INVALIDO"))
                .andExpect(jsonPath("$.correlationId").value("read-failure-1"));
        var batch = importacaoRepository.findAll().get(0);
        Assertions.assertNotNull(batch.getFinalizadoEm());
        Assertions.assertEquals(before + 1, counter("imports.failed", "outcome", "rejected"));
    }

    private double counter(String name, String tag, String value) {
        var counter = registry.find(name).tag(tag, value).counter();
        return counter == null ? 0 : counter.count();
    }

    private void awaitCondition(java.util.function.BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) { if (condition.getAsBoolean()) return; Thread.sleep(20); }
        Assertions.fail("Condição não atingida no tempo esperado");
    }

    private String path(Fixture f) { return "/api/v1/empresas/" + f.empresa() + "/setores/" + f.setor(); }

    private MockMultipartFile workbook(String func, String peca, String qty, String start, String end) throws Exception {
        try (var wb = new XSSFWorkbook(); var out = new ByteArrayOutputStream()) {
            var sheet = wb.createSheet("Operacao");
            var header = sheet.createRow(0);
            String[] headers = {"Data", "Código Funcionário", "Código Peça", "Quantidade", "Hora Início", "Hora Fim"};
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);
            var row = sheet.createRow(1);
            String[] values = {"26/09/2026", func, peca, qty, start, end};
            for (int i = 0; i < values.length; i++) row.createCell(i).setCellValue(values[i]);
            wb.write(out);
            return new MockMultipartFile("file", "producao.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
        }
    }

    private MockMultipartFile workbookRows(String[][] rows) throws Exception {
        try (var wb = new XSSFWorkbook(); var out = new ByteArrayOutputStream()) {
            var sheet = wb.createSheet("Operacao");
            var header = sheet.createRow(0);
            String[] headers = {"Data", "Código Funcionário", "Código Peça", "Quantidade", "Hora Início", "Hora Fim"};
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);
            for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
                var row = sheet.createRow(rowIndex + 1);
                String[] values = {"26/09/2026", rows[rowIndex][0], rows[rowIndex][1], rows[rowIndex][2], rows[rowIndex][3], rows[rowIndex][4]};
                for (int i = 0; i < values.length; i++) row.createCell(i).setCellValue(values[i]);
            }
            wb.write(out);
            return new MockMultipartFile("file", "producao.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
        }
    }

    private void addEmployee(long empresa, String codigo, String nome) throws Exception {
        mvc.perform(post("/api/v1/empresas/" + empresa + "/funcionarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"" + codigo + "\",\"nome\":\"" + nome + "\"}"))
                .andExpect(status().isCreated());
    }

    private void addPart(long empresa, String codigo, String descricao) throws Exception {
        mvc.perform(post("/api/v1/empresas/" + empresa + "/pecas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"" + codigo + "\",\"descricao\":\"" + descricao + "\"}"))
                .andExpect(status().isCreated());
    }

    private Fixture fixture() throws Exception {
        mvc.perform(post("/api/v1/empresas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"razaoSocial\":\"Importadora\",\"nomeFantasia\":\"Importadora\",\"cnpj\":\"48111111111111\"}"))
                .andExpect(status().isCreated());
        long empresa = empresaRepository.findByCnpj("48111111111111").orElseThrow().getId();
        mvc.perform(post("/api/v1/empresas/" + empresa + "/setores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"PINT\",\"nome\":\"Pintura\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/empresas/" + empresa + "/funcionarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"00147\",\"nome\":\"Operador\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/empresas/" + empresa + "/pecas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"54871\",\"descricao\":\"Suporte\"}"))
                .andExpect(status().isCreated());
        return new Fixture(empresa, setorRepository.findByEmpresaIdAndCodigo(empresa, "PINT").orElseThrow().getId());
    }

    private record Fixture(long empresa, long setor) {}
}
