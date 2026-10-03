package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.audit.*;
import com.metalurgica.sistemagestaoindustrial.repository.*;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayOutputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImportacaoExcelIntegrationTests extends AuthenticatedIntegrationTest {
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

    @BeforeEach
    void clean() {
        eventoAuditoriaRepository.deleteAll(); resultadoRepository.deleteAll(); paradaRepository.deleteAll();
        motivoRepository.deleteAll(); producaoRepository.deleteAll(); importacaoRepository.deleteAll();
        maquinaRepository.deleteAll(); turnoRepository.deleteAll(); pecaRepository.deleteAll();
        funcionarioRepository.deleteAll(); setorRepository.deleteAll(); usuarioRepository.deleteAll(); empresaRepository.deleteAll();
    }

    @Test
    void importsValidWorkbookAndPreventsSameHash() throws Exception {
        var f = fixture();
        var file = workbook("00147", "54871", "350", "08:00", "09:00");
        double before = counter("imports.completed", "outcome", "success");
        mvc.perform(multipart(path(f) + "/importacoes/producao").file(file))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("CONCLUIDA"))
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
