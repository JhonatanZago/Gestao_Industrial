package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.repository.EmpresaRepository;
import com.metalurgica.sistemagestaoindustrial.repository.FuncionarioRepository;
import com.metalurgica.sistemagestaoindustrial.repository.PecaRepository;
import com.metalurgica.sistemagestaoindustrial.repository.RegistroProducaoRepository;
import com.metalurgica.sistemagestaoindustrial.repository.ParadaProducaoRepository;
import com.metalurgica.sistemagestaoindustrial.repository.MotivoParadaRepository;
import com.metalurgica.sistemagestaoindustrial.repository.MaquinaRepository;
import com.metalurgica.sistemagestaoindustrial.repository.TurnoRepository;
import com.metalurgica.sistemagestaoindustrial.repository.ImportacaoProducaoRepository;
import com.metalurgica.sistemagestaoindustrial.repository.ResultadoLinhaImportacaoRepository;
import com.metalurgica.sistemagestaoindustrial.repository.SetorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FuncionarioPecaIntegrationTests extends AuthenticatedIntegrationTest {
    @Autowired EmpresaRepository empresaRepository;
    @Autowired FuncionarioRepository funcionarioRepository;
    @Autowired PecaRepository pecaRepository;
    @Autowired RegistroProducaoRepository registroProducaoRepository;
    @Autowired ParadaProducaoRepository paradaRepository;
    @Autowired MotivoParadaRepository motivoRepository;
    @Autowired SetorRepository setorRepository;
    @Autowired MaquinaRepository maquinaRepository;
    @Autowired TurnoRepository turnoRepository;
    @Autowired ImportacaoProducaoRepository importacaoRepository;
    @Autowired ResultadoLinhaImportacaoRepository resultadoImportacaoRepository;

    @BeforeEach
    void clean() { resultadoImportacaoRepository.deleteAll(); registroProducaoRepository.deleteAll(); importacaoRepository.deleteAll(); paradaRepository.deleteAll(); motivoRepository.deleteAll(); maquinaRepository.deleteAll(); turnoRepository.deleteAll(); pecaRepository.deleteAll(); funcionarioRepository.deleteAll(); setorRepository.deleteAll(); usuarioRepository.deleteAll(); empresaRepository.deleteAll(); }

    @Test
    void preservesEmployeeCodeAndManagesLifecycle() throws Exception {
        var empresa = createEmpresa("A", "31111111111111");
        createFuncionario(empresa, " 00147 ", " Maria Silva ");
        var id = funcionarioRepository.findAll().get(0).getId();
        mvc.perform(get("/api/v1/empresas/" + empresa + "/funcionarios/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("00147")).andExpect(jsonPath("$.nome").value("Maria Silva"));
        mvc.perform(patch("/api/v1/empresas/" + empresa + "/funcionarios/" + id + "/desativar")).andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(false));
        mvc.perform(patch("/api/v1/empresas/" + empresa + "/funcionarios/" + id + "/ativar")).andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void employeesAreUniqueInsideCompanyAndCanRepeatAcrossCompanies() throws Exception {
        var a = createEmpresa("A", "32111111111111"); var b = createEmpresa("B", "32222222222222");
        createFuncionario(a, "00147", "Ana"); createFuncionario(b, "00147", "Bia");
        mvc.perform(post("/api/v1/empresas/" + a + "/funcionarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"00147\",\"nome\":\"Outra\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value(containsString("00147")));
    }

    @Test
    void employeeIsolationBlocksGetUpdateAndStatusThroughWrongCompany() throws Exception {
        var a = createEmpresa("A", "33111111111111"); var b = createEmpresa("B", "33222222222222"); createFuncionario(b, "00231", "Carlos");
        var id = funcionarioRepository.findAll().get(0).getId();
        mvc.perform(get("/api/v1/empresas/" + a + "/funcionarios/" + id)).andExpect(status().isNotFound());
        mvc.perform(put("/api/v1/empresas/" + a + "/funcionarios/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"00999\",\"nome\":\"Alterado\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(patch("/api/v1/empresas/" + a + "/funcionarios/" + id + "/desativar")).andExpect(status().isNotFound());
    }

    @Test
    void acceptsTextPartCodesAndDoesNotRequireSector() throws Exception {
        var empresa = createEmpresa("A", "34111111111111");
        createPeca(empresa, " 0054871 ", "Suporte lateral");
        createPeca(empresa, "ABC-1020", "Peca alfanumerica");
        var peca = pecaRepository.findAll().get(0);
        mvc.perform(get("/api/v1/empresas/" + empresa + "/pecas/" + peca.getId())).andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("0054871"));
    }

    @Test
    void partsAreUniqueInsideCompanyAndCanRepeatAcrossCompanies() throws Exception {
        var a = createEmpresa("A", "35111111111111"); var b = createEmpresa("B", "35222222222222");
        createPeca(a, "54871", "A"); createPeca(b, "54871", "B");
        mvc.perform(post("/api/v1/empresas/" + a + "/pecas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"54871\",\"descricao\":\"Duplicada\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value(containsString("54871")));
    }

    @Test
    void partIsolationAndValidationWork() throws Exception {
        var a = createEmpresa("A", "36111111111111"); var b = createEmpresa("B", "36222222222222"); createPeca(b, "54871-A", "B");
        var id = pecaRepository.findAll().get(0).getId();
        mvc.perform(get("/api/v1/empresas/" + a + "/pecas/" + id)).andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/empresas/999/pecas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"X\",\"descricao\":\"X\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/empresas/" + a + "/pecas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"\",\"descricao\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    private long createEmpresa(String nome, String cnpj) throws Exception {
        mvc.perform(post("/api/v1/empresas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"razaoSocial\":\"Empresa " + nome + "\",\"nomeFantasia\":\"" + nome + "\",\"cnpj\":\"" + cnpj + "\"}"))
                .andExpect(status().isCreated());
        return empresaRepository.findByCnpj(cnpj).orElseThrow().getId();
    }

    private void createFuncionario(long empresa, String codigo, String nome) throws Exception {
        mvc.perform(post("/api/v1/empresas/" + empresa + "/funcionarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"" + codigo + "\",\"nome\":\"" + nome + "\"}"))
                .andExpect(status().isCreated());
    }

    private void createPeca(long empresa, String codigo, String descricao) throws Exception {
        mvc.perform(post("/api/v1/empresas/" + empresa + "/pecas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"" + codigo + "\",\"descricao\":\"" + descricao + "\"}"))
                .andExpect(status().isCreated());
    }
}
