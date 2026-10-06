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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmpresaSetorIntegrationTests extends AuthenticatedIntegrationTest {
    @Autowired SetorRepository setorRepository;
    @Autowired FuncionarioRepository funcionarioRepository;
    @Autowired PecaRepository pecaRepository;
    @Autowired RegistroProducaoRepository registroProducaoRepository;
    @Autowired ParadaProducaoRepository paradaRepository;
    @Autowired MotivoParadaRepository motivoRepository;
    @Autowired EmpresaRepository empresaRepository;
    @Autowired MaquinaRepository maquinaRepository;
    @Autowired TurnoRepository turnoRepository;
    @Autowired ImportacaoProducaoRepository importacaoRepository;
    @Autowired ResultadoLinhaImportacaoRepository resultadoImportacaoRepository;

    @BeforeEach
    void clean() { resultadoImportacaoRepository.deleteAll(); registroProducaoRepository.deleteAll(); importacaoRepository.deleteAll(); paradaRepository.deleteAll(); motivoRepository.deleteAll(); maquinaRepository.deleteAll(); turnoRepository.deleteAll(); pecaRepository.deleteAll(); funcionarioRepository.deleteAll(); setorRepository.deleteAll(); usuarioRepository.deleteAll(); empresaRepository.deleteAll(); }

    @Test
    void createsEmpresaWithNormalizedCnpjAndManagesLifecycle() throws Exception {
        mvc.perform(post("/api/v1/empresas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"razaoSocial\":\"  Metalúrgica A  \",\"nomeFantasia\":\"  A  \",\"cnpj\":\"12.345.678/0001-90\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.cnpj").value("12345678000190"))
                .andExpect(jsonPath("$.ativo").value(true));
        var id = empresaRepository.findByCnpj("12345678000190").orElseThrow().getId();
        mvc.perform(patch("/api/v1/empresas/" + id + "/desativar")).andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(false));
        mvc.perform(patch("/api/v1/empresas/" + id + "/ativar")).andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void allowsSameSectorCodeAcrossCompaniesButRejectsDuplicateInSameCompany() throws Exception {
        var a = createEmpresa("A", "11111111111111");
        var b = createEmpresa("B", "22222222222222");
        createSetor(a, " pint ", " Pintura a pó ");
        createSetor(b, "PINT", "Pintura B");
        mvc.perform(post("/api/v1/empresas/" + a + "/setores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"PINT\",\"nome\":\"Outro\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value(containsString("PINT")));
    }

    @Test
    void preventsCrossCompanySectorAccess() throws Exception {
        var a = createEmpresa("A", "11111111111111"); var b = createEmpresa("B", "22222222222222"); createSetor(b, "LASER", "Laser");
        var setorId = setorRepository.findAll().get(0).getId();
        mvc.perform(get("/api/v1/empresas/" + a + "/setores/" + setorId)).andExpect(status().isForbidden());
    }

    @Test
    void rejectsInvalidEmpresaAndUnknownEmpresaSector() throws Exception {
        mvc.perform(post("/api/v1/empresas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"razaoSocial\":\"\",\"nomeFantasia\":\"A\",\"cnpj\":\"123\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.razaoSocial").exists());
        createEmpresa("A", "33333333333333");
        mvc.perform(post("/api/v1/empresas/999/setores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"PINT\",\"nome\":\"Pintura\"}"))
                .andExpect(status().isForbidden());
    }

    private long createEmpresa(String nome, String cnpj) throws Exception {
        mvc.perform(post("/api/v1/empresas").contentType(MediaType.APPLICATION_JSON)
                .content("{\"razaoSocial\":\"Empresa " + nome + "\",\"nomeFantasia\":\"" + nome + "\",\"cnpj\":\"" + cnpj + "\"}"))
                .andExpect(status().isCreated());
        return empresaRepository.findByCnpj(cnpj).orElseThrow().getId();
    }

    private void createSetor(long empresaId, String codigo, String nome) throws Exception {
        mvc.perform(post("/api/v1/empresas/" + empresaId + "/setores").contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\":\"" + codigo + "\",\"nome\":\"" + nome + "\"}"))
                .andExpect(status().isCreated());
    }
}
