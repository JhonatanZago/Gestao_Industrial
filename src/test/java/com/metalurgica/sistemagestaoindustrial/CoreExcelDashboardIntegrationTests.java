package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CoreExcelDashboardIntegrationTests extends AuthenticatedIntegrationTest {
    @Autowired EmpresaRepository empresaRepository;
    @Autowired SetorRepository setorRepository;
    @Autowired FuncionarioRepository funcionarioRepository;
    @Autowired PecaRepository pecaRepository;
    @Autowired RegistroProducaoRepository producaoRepository;
    @Autowired ImportacaoProducaoRepository importacaoRepository;
    @Autowired ResultadoLinhaImportacaoRepository resultadoRepository;
    @Autowired ParadaProducaoRepository paradaRepository;
    @Autowired MotivoParadaRepository motivoRepository;

    @BeforeEach
    void clean() {
        resultadoRepository.deleteAll(); paradaRepository.deleteAll(); producaoRepository.deleteAll(); importacaoRepository.deleteAll(); motivoRepository.deleteAll();
        pecaRepository.deleteAll(); funcionarioRepository.deleteAll(); setorRepository.deleteAll();
        usuarioRepository.deleteAll(); empresaRepository.deleteAll();
    }

    @Test
    void percorreTemplateImportacaoDashboardsDrilldownsERelatorio() throws Exception {
        long empresa = createCompany();
        long setor = setorRepository.findByEmpresaIdAndCodigo(empresa, "PINT").orElseThrow().getId();
        var template = new MockMultipartFile("file", "PRODUCAO_PADRAO.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                Files.readAllBytes(Path.of("docs/templates/PRODUCAO_PADRAO.xlsx")));
        var path = "/api/v1/empresas/" + empresa + "/setores/" + setor;
        mvc.perform(multipart(path + "/importacoes/producao").file(template))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.linhasImportadas").value(3));
        var period = "inicio=2026-09-29T00:00:00&fim=2026-09-29T23:59:59";
        mvc.perform(get("/api/v1/empresas/" + empresa + "/analytics/dashboard?" + period))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantidadeProduzida").value(350))
                .andExpect(jsonPath("$.duracaoRegistradaMinutos").value(185))
                .andExpect(jsonPath("$.setoresComDados").value(1))
                .andExpect(jsonPath("$.funcionariosEnvolvidos").value(2))
                .andExpect(jsonPath("$.codigosPecaProduzidos").value(2));
        mvc.perform(get("/api/v1/empresas/" + empresa + "/analytics/dashboard/setor/" + setor + "?" + period))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantidadeProduzida").value(350));
        long funcionario = funcionarioRepository.findByEmpresaIdAndCodigo(empresa, "00147").orElseThrow().getId();
        long peca = pecaRepository.findByEmpresaIdAndCodigo(empresa, "PEC-001").orElseThrow().getId();
        mvc.perform(get("/api/v1/empresas/" + empresa + "/analytics/funcionarios/" + funcionario + "?" + period))
                .andExpect(status().isOk()).andExpect(jsonPath("$.codigo").value("00147"))
                .andExpect(jsonPath("$.quantidadeProduzida").value(200));
        mvc.perform(get("/api/v1/empresas/" + empresa + "/analytics/pecas?" + period))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].codigo").value("PEC-001"))
                .andExpect(jsonPath("$.content[0].quantidadeProduzida").value(270));
        mvc.perform(get("/api/v1/empresas/" + empresa + "/relatorios/simplificado?" + period))
                .andExpect(status().isOk()).andExpect(jsonPath("$.dashboard.quantidadeProduzida").value(350))
                .andExpect(jsonPath("$.dashboard.duracaoRegistradaMinutos").value(185));
        org.junit.jupiter.api.Assertions.assertEquals(3, producaoRepository.count());
    }

    private long createCompany() throws Exception {
        mvc.perform(post("/api/v1/empresas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"razaoSocial\":\"Fabrica Core\",\"nomeFantasia\":\"Fabrica Core\",\"cnpj\":\"47777777777777\"}"))
                .andExpect(status().isCreated());
        long empresa = empresaRepository.findByCnpj("47777777777777").orElseThrow().getId();
        mvc.perform(post("/api/v1/empresas/" + empresa + "/setores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"PINT\",\"nome\":\"Pintura a pó\"}"))
                .andExpect(status().isCreated());
        for (String employee : new String[]{"00147", "00152"})
            mvc.perform(post("/api/v1/empresas/" + empresa + "/funcionarios").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"codigo\":\"" + employee + "\",\"nome\":\"Operador " + employee + "\"}"))
                    .andExpect(status().isCreated());
        for (String part : new String[]{"PEC-001", "PEC-002"})
            mvc.perform(post("/api/v1/empresas/" + empresa + "/pecas").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"codigo\":\"" + part + "\",\"descricao\":\"Peca " + part + "\"}"))
                    .andExpect(status().isCreated());
        return empresa;
    }
}
