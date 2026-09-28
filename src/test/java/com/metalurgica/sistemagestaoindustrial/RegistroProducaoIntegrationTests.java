package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.audit.*;
import com.metalurgica.sistemagestaoindustrial.repository.*;
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
class RegistroProducaoIntegrationTests extends AuthenticatedIntegrationTest {
    @Autowired EmpresaRepository empresaRepository;
    @Autowired SetorRepository setorRepository;
    @Autowired FuncionarioRepository funcionarioRepository;
    @Autowired PecaRepository pecaRepository;
    @Autowired RegistroProducaoRepository producaoRepository;
    @Autowired ParadaProducaoRepository paradaRepository;
    @Autowired MotivoParadaRepository motivoRepository;
    @Autowired MaquinaRepository maquinaRepository;
    @Autowired TurnoRepository turnoRepository;
    @Autowired ImportacaoProducaoRepository importacaoRepository;
    @Autowired ResultadoLinhaImportacaoRepository resultadoImportacaoRepository;
    @Autowired EventoAuditoriaRepository eventoAuditoriaRepository;

    @BeforeEach
    void clean() { eventoAuditoriaRepository.deleteAll(); resultadoImportacaoRepository.deleteAll(); importacaoRepository.deleteAll(); paradaRepository.deleteAll(); motivoRepository.deleteAll(); producaoRepository.deleteAll(); maquinaRepository.deleteAll(); turnoRepository.deleteAll(); pecaRepository.deleteAll(); funcionarioRepository.deleteAll(); setorRepository.deleteAll(); usuarioRepository.deleteAll(); empresaRepository.deleteAll(); }

    @Test
    void createsProductionAndCalculatesEightyEightMinutes() throws Exception {
        var f = fixture("A", "41111111111111", "00147", "54871");
        mvc.perform(post(path(f.empresa()) ).contentType(MediaType.APPLICATION_JSON)
                .content(productionJson(f, "350", "2026-09-26T08:14:00", "2026-09-26T09:42:00")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.quantidade").value(350))
                .andExpect(jsonPath("$.duracaoBrutaMinutos").value(88)).andExpect(jsonPath("$.funcionarioCodigo").value("00147"));
        var event = eventoAuditoriaRepository.findAll().stream().filter(e -> e.getTipoRecurso() == TipoRecursoAuditoria.REGISTRO_PRODUCAO).findFirst().orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(AcaoAuditoria.CRIACAO, event.getAcao());
        org.junit.jupiter.api.Assertions.assertTrue(event.getDadosNovos().contains("350"));
        mvc.perform(get("/api/v1/empresas/" + f.empresa() + "/auditoria?tipoRecurso=REGISTRO_PRODUCAO&page=0&size=10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].tipoRecurso").value("REGISTRO_PRODUCAO"));
        mvc.perform(get("/api/v1/empresas/" + f.empresa() + "/auditoria/REGISTRO_PRODUCAO/" + event.getRecursoId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].alteracoes").isArray());
        mvc.perform(get("/api/v1/empresas/" + f.empresa() + "/auditoria?page=0&size=101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void acceptsMidnightAndRejectsInvalidQuantityAndTimes() throws Exception {
        var f = fixture("A", "42111111111111", "00147", "54871");
        mvc.perform(post(path(f.empresa())).contentType(MediaType.APPLICATION_JSON).content(productionJson(f, "1", "2026-09-26T22:00:00", "2026-09-27T02:00:00")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.duracaoBrutaMinutos").value(240));
        mvc.perform(post(path(f.empresa())).contentType(MediaType.APPLICATION_JSON).content(productionJson(f, "0", "2026-09-27T03:00:00", "2026-09-27T04:00:00"))).andExpect(status().isBadRequest());
        mvc.perform(post(path(f.empresa())).contentType(MediaType.APPLICATION_JSON).content(productionJson(f, "1", "2026-09-27T04:00:00", "2026-09-27T04:00:00"))).andExpect(status().isBadRequest());
    }

    @Test
    void rejectsOverlapsAllowsAdjacentAndDifferentEmployees() throws Exception {
        var f = fixture("A", "43111111111111", "00147", "54871"); var other = createFuncionario(f.empresa(), "00231");
        createProduction(f, "1", "2026-09-26T08:00:00", "2026-09-26T10:00:00");
        mvc.perform(post(path(f.empresa())).contentType(MediaType.APPLICATION_JSON).content(productionJson(f, "1", "2026-09-26T09:30:00", "2026-09-26T11:00:00"))).andExpect(status().isConflict());
        mvc.perform(post(path(f.empresa())).contentType(MediaType.APPLICATION_JSON).content(productionJson(f, "1", "2026-09-26T10:00:00", "2026-09-26T11:00:00"))).andExpect(status().isCreated());
        var different = new Fixture(f.empresa(), f.setor(), other, f.peca());
        mvc.perform(post(path(f.empresa())).contentType(MediaType.APPLICATION_JSON).content(productionJson(different, "1", "2026-09-26T09:30:00", "2026-09-26T11:00:00"))).andExpect(status().isCreated());
    }

    @Test
    void rejectsContainedAndEnvelopingOverlaps() throws Exception {
        var f = fixture("A", "43511111111111", "00147", "54871");
        createProduction(f, "1", "2026-09-26T08:00:00", "2026-09-26T12:00:00");
        mvc.perform(post(path(f.empresa())).contentType(MediaType.APPLICATION_JSON).content(productionJson(f, "1", "2026-09-26T09:00:00", "2026-09-26T10:00:00"))).andExpect(status().isConflict());
        mvc.perform(post(path(f.empresa())).contentType(MediaType.APPLICATION_JSON).content(productionJson(f, "1", "2026-09-26T07:00:00", "2026-09-26T13:00:00"))).andExpect(status().isConflict());
    }

    @Test
    void rejectsWrongCompanyAndInactiveResources() throws Exception {
        var a = fixture("A", "44111111111111", "00147", "54871"); var b = fixture("B", "44222222222222", "00147", "54871");
        mvc.perform(post(path(a.empresa())).contentType(MediaType.APPLICATION_JSON).content(productionJson(b, "1", "2026-09-26T08:00:00", "2026-09-26T09:00:00"))).andExpect(status().isNotFound());
        mvc.perform(patch("/api/v1/empresas/" + a.empresa() + "/funcionarios/" + a.funcionario() + "/desativar")).andExpect(status().isOk());
        mvc.perform(post(path(a.empresa())).contentType(MediaType.APPLICATION_JSON).content(productionJson(a, "1", "2026-09-26T08:00:00", "2026-09-26T09:00:00"))).andExpect(status().isConflict()).andExpect(jsonPath("$.message").value(containsString("inativo")));
    }

    @Test
    void rejectsInactiveSectorAndPartForNewProduction() throws Exception {
        var f = fixture("A", "44511111111111", "00147", "54871");
        var setor = setorRepository.findById(f.setor()).orElseThrow(); setor.setAtivo(false); setorRepository.save(setor);
        mvc.perform(post(path(f.empresa())).contentType(MediaType.APPLICATION_JSON).content(productionJson(f, "1", "2026-09-26T08:00:00", "2026-09-26T09:00:00"))).andExpect(status().isConflict()).andExpect(jsonPath("$.message").value(containsString("setor")));
        setor.setAtivo(true); setorRepository.save(setor);
        var peca = pecaRepository.findById(f.peca()).orElseThrow(); peca.setAtivo(false); pecaRepository.save(peca);
        mvc.perform(post(path(f.empresa())).contentType(MediaType.APPLICATION_JSON).content(productionJson(f, "1", "2026-09-26T08:00:00", "2026-09-26T09:00:00"))).andExpect(status().isConflict()).andExpect(jsonPath("$.message").value(containsString("peça")));
    }

    @Test
    void updatesIgnoringItselfFiltersPaginatesAndUsesLogicalLifecycle() throws Exception {
        var f = fixture("A", "45111111111111", "00147", "54871");
        createProduction(f, "10", "2026-09-26T08:00:00", "2026-09-26T09:00:00");
        var id = producaoRepository.findAll().get(0).getId();
        mvc.perform(put(path(f.empresa()) + "/" + id).contentType(MediaType.APPLICATION_JSON).content(productionJson(f, "11", "2026-09-26T08:00:00", "2026-09-26T09:00:00"))).andExpect(status().isOk()).andExpect(jsonPath("$.quantidade").value(11));
        var updateEvent = eventoAuditoriaRepository.findAll().stream().filter(e -> e.getTipoRecurso() == TipoRecursoAuditoria.REGISTRO_PRODUCAO && e.getRecursoId().equals(id) && e.getAcao() == AcaoAuditoria.ALTERACAO).findFirst().orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(updateEvent.getDadosAnteriores().contains("10"));
        org.junit.jupiter.api.Assertions.assertTrue(updateEvent.getDadosNovos().contains("11"));
        mvc.perform(get(path(f.empresa()) + "?funcionarioId=" + f.funcionario() + "&page=0&size=1")).andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(1));
        mvc.perform(patch(path(f.empresa()) + "/" + id + "/desativar")).andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(false));
        mvc.perform(get(path(f.empresa()) + "/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(false));
    }

    private String path(long empresa) { return "/api/v1/empresas/" + empresa + "/producoes"; }
    private String productionJson(Fixture f, String quantidade, String inicio, String fim) {
        return "{\"setorId\":" + f.setor() + ",\"funcionarioId\":" + f.funcionario() + ",\"pecaId\":" + f.peca() + ",\"quantidade\":" + quantidade + ",\"inicio\":\"" + inicio + "\",\"fim\":\"" + fim + "\",\"observacao\":\"Produção normal\"}";
    }
    private void createProduction(Fixture f, String quantidade, String inicio, String fim) throws Exception {
        mvc.perform(post(path(f.empresa())).contentType(MediaType.APPLICATION_JSON).content(productionJson(f, quantidade, inicio, fim))).andExpect(status().isCreated());
    }
    private long createFuncionario(long empresa, String codigo) throws Exception {
        mvc.perform(post("/api/v1/empresas/" + empresa + "/funcionarios").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"" + codigo + "\",\"nome\":\"Auxiliar\"}"))
                .andExpect(status().isCreated());
        return funcionarioRepository.findByEmpresaIdAndCodigo(empresa, codigo).orElseThrow().getId();
    }
    private Fixture fixture(String nome, String cnpj, String funcionarioCodigo, String pecaCodigo) throws Exception {
        mvc.perform(post("/api/v1/empresas").contentType(MediaType.APPLICATION_JSON).content("{\"razaoSocial\":\"Empresa " + nome + "\",\"nomeFantasia\":\"" + nome + "\",\"cnpj\":\"" + cnpj + "\"}"))
                .andExpect(status().isCreated());
        var empresa = empresaRepository.findByCnpj(cnpj).orElseThrow().getId();
        mvc.perform(post("/api/v1/empresas/" + empresa + "/setores").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"PINT\",\"nome\":\"Pintura\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/empresas/" + empresa + "/funcionarios").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"" + funcionarioCodigo + "\",\"nome\":\"Operador\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/empresas/" + empresa + "/pecas").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"" + pecaCodigo + "\",\"descricao\":\"Suporte\"}"))
                .andExpect(status().isCreated());
        return new Fixture(empresa, setorRepository.findByEmpresaIdAndCodigo(empresa, "PINT").orElseThrow().getId(),
                funcionarioRepository.findByEmpresaIdAndCodigo(empresa, funcionarioCodigo).orElseThrow().getId(),
                pecaRepository.findByEmpresaIdAndCodigo(empresa, pecaCodigo).orElseThrow().getId());
    }
    private record Fixture(long empresa, long setor, long funcionario, long peca) { }
}
