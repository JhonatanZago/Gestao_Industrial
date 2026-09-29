package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.audit.EventoAuditoriaRepository;
import com.metalurgica.sistemagestaoindustrial.domain.StatusImportacao;
import com.metalurgica.sistemagestaoindustrial.repository.*;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.io.ByteArrayOutputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:importfailure;MODE=PostgreSQL;DB_CLOSE_DELAY=0;DATABASE_TO_LOWER=TRUE")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImportacaoFailureIntegrationTests extends AuthenticatedIntegrationTest {
    @Autowired SetorRepository setorRepository;
    @Autowired FuncionarioRepository funcionarioRepository;
    @Autowired PecaRepository pecaRepository;
    @Autowired ImportacaoProducaoRepository importacaoRepository;
    @Autowired ResultadoLinhaImportacaoRepository resultadoRepository;
    @Autowired EventoAuditoriaRepository eventoAuditoriaRepository;
    @Autowired MeterRegistry registry;
    @MockitoSpyBean RegistroProducaoRepository producaoRepository;

    @BeforeEach
    void clean() {
        eventoAuditoriaRepository.deleteAll();
        resultadoRepository.deleteAll();
        producaoRepository.deleteAll();
        importacaoRepository.deleteAll();
        pecaRepository.deleteAll();
        funcionarioRepository.deleteAll();
        setorRepository.deleteAll();
        usuarioRepository.deleteAll();
        empresaRepository.deleteAll();
    }

    @Test
    void preservesFailedBatchAndRollsBackProductionInIndependentTransaction() throws Exception {
        long empresaId = createFixture();
        long setorId = setorRepository.findByEmpresaIdAndCodigo(empresaId, "PINT").orElseThrow().getId();
        double failedBefore = counter("imports.failed", "outcome", "failed");
        double startedBefore = counter("imports.started", null, null);
        doThrow(new DataIntegrityViolationException("forced-test-failure"))
                .when(producaoRepository).save(any());

        mvc.perform(multipart("/api/v1/empresas/" + empresaId + "/setores/" + setorId + "/importacoes/producao")
                        .file(workbook()).header("X-Correlation-Id", "failure-test-1"))
                .andExpect(status().isInternalServerError())
                .andExpect(header().string("X-Correlation-Id", "failure-test-1"))
                .andExpect(jsonPath("$.code").value("IMPORT_ERROR"))
                .andExpect(jsonPath("$.correlationId").value("failure-test-1"));

        var batch = importacaoRepository.findAll().get(0);
        Assertions.assertEquals(StatusImportacao.FALHOU, batch.getStatus());
        Assertions.assertEquals("IMPORT_PERSISTENCE_FAILURE", batch.getErroCodigo());
        Assertions.assertEquals("failure-test-1", batch.getCorrelationId());
        Assertions.assertNotNull(batch.getFinalizadoEm());
        Assertions.assertEquals(0, producaoRepository.count());
        Assertions.assertEquals(startedBefore + 1, counter("imports.started", null, null));
        Assertions.assertEquals(failedBefore + 1, counter("imports.failed", "outcome", "failed"));
    }

    private double counter(String name, String tag, String value) {
        var search = registry.find(name);
        var counter = tag == null ? search.counter() : search.tag(tag, value).counter();
        return counter == null ? 0 : counter.count();
    }

    private long createFixture() throws Exception {
        mvc.perform(post("/api/v1/empresas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"razaoSocial\":\"Importadora Falha\",\"nomeFantasia\":\"Importadora Falha\",\"cnpj\":\"48999999999999\"}"))
                .andExpect(status().isCreated());
        long empresaId = empresaRepository.findByCnpj("48999999999999").orElseThrow().getId();
        mvc.perform(post("/api/v1/empresas/" + empresaId + "/setores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"PINT\",\"nome\":\"Pintura\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/empresas/" + empresaId + "/funcionarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"00147\",\"nome\":\"Operador\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/empresas/" + empresaId + "/pecas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"54871\",\"descricao\":\"Suporte\"}"))
                .andExpect(status().isCreated());
        return empresaId;
    }

    private MockMultipartFile workbook() throws Exception {
        try (var workbook = new XSSFWorkbook(); var out = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Operacao");
            var header = sheet.createRow(0);
            String[] headers = {"Data", "Cod. Funcionário", "Cód. Peça", "Qtd", "Hora Início", "Hora Fim"};
            for (int index = 0; index < headers.length; index++) header.createCell(index).setCellValue(headers[index]);
            var row = sheet.createRow(1);
            row.createCell(0).setCellValue("26/09/2026");
            row.createCell(1).setCellValue("00147");
            row.createCell(2).setCellValue("54871");
            row.createCell(3).setCellValue("350");
            row.createCell(4).setCellValue("08:00");
            row.createCell(5).setCellValue("09:00");
            workbook.write(out);
            return new MockMultipartFile("file", "failure.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
        }
    }
}
