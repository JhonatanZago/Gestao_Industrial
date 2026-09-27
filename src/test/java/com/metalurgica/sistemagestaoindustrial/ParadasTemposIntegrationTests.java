package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ParadasTemposIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired EmpresaRepository empresaRepository;
    @Autowired SetorRepository setorRepository;
    @Autowired FuncionarioRepository funcionarioRepository;
    @Autowired PecaRepository pecaRepository;
    @Autowired RegistroProducaoRepository producaoRepository;
    @Autowired MotivoParadaRepository motivoRepository;
    @Autowired ParadaProducaoRepository paradaRepository;
    @Autowired MaquinaRepository maquinaRepository;
    @Autowired TurnoRepository turnoRepository;

    @BeforeEach
    void clean() { paradaRepository.deleteAll(); motivoRepository.deleteAll(); producaoRepository.deleteAll(); maquinaRepository.deleteAll(); turnoRepository.deleteAll(); pecaRepository.deleteAll(); funcionarioRepository.deleteAll(); setorRepository.deleteAll(); empresaRepository.deleteAll(); }

    @Test
    void calculatesActiveStoppedAndProductiveMinutesAndKeepsHistory() throws Exception {
        var f = fixture("A", "46111111111111");
        var motivo = createMotivo(f.empresa(), "  FALTA-MATERIAL ");
        var prod = createProduction(f);
        var p1 = createParada(f, prod, motivo, "2026-09-26T09:10:00", "2026-09-26T09:30:00");
        var p2 = createParada(f, prod, motivo, "2026-09-26T10:45:00", "2026-09-26T11:00:00");

        getProduction(f, prod).andExpect(jsonPath("$.duracaoBrutaMinutos").value(240)).andExpect(jsonPath("$.tempoParadoMinutos").value(35)).andExpect(jsonPath("$.tempoProdutivoMinutos").value(205));
        mvc.perform(patch(paradaPath(f, prod, p2) + "/desativar")).andExpect(status().isOk());
        getProduction(f, prod).andExpect(jsonPath("$.tempoParadoMinutos").value(20)).andExpect(jsonPath("$.tempoProdutivoMinutos").value(220));
        mvc.perform(patch(paradaPath(f, prod, p2) + "/ativar")).andExpect(status().isOk());
        mvc.perform(patch(path(f.empresa(), prod) + "/desativar")).andExpect(status().isOk());
        mvc.perform(get(paradaPath(f, prod, p1))).andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(true));
        mvc.perform(get(path(f.empresa(), prod))).andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(false));
    }

    @Test
    void validatesPeriodsOverlapAdjacencyAndLifecycle() throws Exception {
        var f = fixture("B", "46222222222222");
        var motivo = createMotivo(f.empresa(), "PARADA");
        var prod = createProduction(f);
        createParada(f, prod, motivo, "2026-09-26T08:00:00", "2026-09-26T08:10:00");
        createParada(f, prod, motivo, "2026-09-26T11:45:00", "2026-09-26T12:00:00");
        parada(f, prod, motivo, "2026-09-26T08:05:00", "2026-09-26T08:15:00").andExpect(status().isConflict());
        parada(f, prod, motivo, "2026-09-26T09:00:00", "2026-09-26T09:00:00").andExpect(status().isBadRequest());
        parada(f, prod, motivo, "2026-09-26T07:59:00", "2026-09-26T08:05:00").andExpect(status().isConflict());
        parada(f, prod, motivo, "2026-09-26T08:10:00", "2026-09-26T08:20:00").andExpect(status().isCreated());
        mvc.perform(patch("/api/v1/empresas/" + f.empresa() + "/motivos-parada/" + motivo + "/desativar")).andExpect(status().isOk());
        parada(f, prod, motivo, "2026-09-26T09:30:00", "2026-09-26T09:40:00").andExpect(status().isConflict());
    }

    @Test
    void isolatesReasonsAndRejectsProductionPeriodShrinkWithStops() throws Exception {
        var a = fixture("C", "46333333333333"); var b = fixture("D", "46444444444444");
        var motivoA = createMotivo(a.empresa(), "MATERIAL");
        mvc.perform(post("/api/v1/empresas/" + a.empresa() + "/motivos-parada").contentType(MediaType.APPLICATION_JSON).content(motivoJson("MATERIAL", "Outro"))).andExpect(status().isConflict());
        var motivoB = createMotivo(b.empresa(), "MATERIAL");
        var prod = createProduction(a);
        var parada = createParada(a, prod, motivoA, "2026-09-26T10:00:00", "2026-09-26T10:30:00");
        mvc.perform(get("/api/v1/empresas/" + a.empresa() + "/motivos-parada/" + motivoB)).andExpect(status().isNotFound());
        mvc.perform(put(path(a.empresa(), prod)).contentType(MediaType.APPLICATION_JSON).content(productionJson(a, "1", "2026-09-26T08:00:00", "2026-09-26T10:15:00"))).andExpect(status().isConflict());
        mvc.perform(put(path(a.empresa(), prod)).contentType(MediaType.APPLICATION_JSON).content(productionJson(a, "1", "2026-09-26T08:00:00", "2026-09-26T10:30:00"))).andExpect(status().isOk());
        mvc.perform(put(paradaPath(a, prod, parada)).contentType(MediaType.APPLICATION_JSON).content(paradaJson(motivoA, "2026-09-26T10:00:00", "2026-09-26T10:20:00"))).andExpect(status().isOk());
    }

    private org.springframework.test.web.servlet.ResultActions getProduction(Fixture f, long prod) throws Exception { return mvc.perform(get(path(f.empresa(), prod))).andExpect(status().isOk()); }
    private org.springframework.test.web.servlet.ResultActions parada(Fixture f, long prod, long motivo, String inicio, String fim) throws Exception { return mvc.perform(post(paradas(f, prod)).contentType(MediaType.APPLICATION_JSON).content(paradaJson(motivo, inicio, fim))); }
    private long createParada(Fixture f, long prod, long motivo, String inicio, String fim) throws Exception { parada(f, prod, motivo, inicio, fim).andExpect(status().isCreated()); return paradaRepository.findAll().stream().filter(p -> p.getRegistroProducao().getId().equals(prod)).max(java.util.Comparator.comparing(com.metalurgica.sistemagestaoindustrial.domain.ParadaProducao::getId)).orElseThrow().getId(); }
    private long createMotivo(long empresa, String codigo) throws Exception { mvc.perform(post("/api/v1/empresas/" + empresa + "/motivos-parada").contentType(MediaType.APPLICATION_JSON).content(motivoJson(codigo, "Falta de material"))).andExpect(status().isCreated()); return motivoRepository.findByEmpresaIdAndCodigo(empresa, codigo.trim().toUpperCase()).orElseThrow().getId(); }
    private long createProduction(Fixture f) throws Exception { mvc.perform(post("/api/v1/empresas/" + f.empresa() + "/producoes").contentType(MediaType.APPLICATION_JSON).content(productionJson(f, "1", "2026-09-26T08:00:00", "2026-09-26T12:00:00"))).andExpect(status().isCreated()); return producaoRepository.findAll().get(0).getId(); }
    private Fixture fixture(String nome, String cnpj) throws Exception { mvc.perform(post("/api/v1/empresas").contentType(MediaType.APPLICATION_JSON).content("{\"razaoSocial\":\"Empresa " + nome + "\",\"nomeFantasia\":\"" + nome + "\",\"cnpj\":\"" + cnpj + "\"}")).andExpect(status().isCreated()); long e = empresaRepository.findByCnpj(cnpj).orElseThrow().getId(); mvc.perform(post("/api/v1/empresas/" + e + "/setores").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"PINT\",\"nome\":\"Pintura\"}")).andExpect(status().isCreated()); mvc.perform(post("/api/v1/empresas/" + e + "/funcionarios").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"00147\",\"nome\":\"Operador\"}")).andExpect(status().isCreated()); mvc.perform(post("/api/v1/empresas/" + e + "/pecas").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"54871\",\"descricao\":\"Suporte\"}")).andExpect(status().isCreated()); return new Fixture(e, setorRepository.findByEmpresaIdAndCodigo(e, "PINT").orElseThrow().getId(), funcionarioRepository.findByEmpresaIdAndCodigo(e, "00147").orElseThrow().getId(), pecaRepository.findByEmpresaIdAndCodigo(e, "54871").orElseThrow().getId()); }
    private String path(long e, long p) { return "/api/v1/empresas/" + e + "/producoes/" + p; }
    private String paradas(Fixture f, long p) { return path(f.empresa(), p) + "/paradas"; }
    private String paradaPath(Fixture f, long p, long id) { return paradas(f, p) + "/" + id; }
    private String motivoJson(String codigo, String nome) { return "{\"codigo\":\"" + codigo + "\",\"nome\":\"" + nome + "\",\"categoria\":\"OPERACIONAL\"}"; }
    private String paradaJson(long motivo, String inicio, String fim) { return "{\"motivoParadaId\":" + motivo + ",\"inicio\":\"" + inicio + "\",\"fim\":\"" + fim + "\",\"observacao\":\"Teste\"}"; }
    private String productionJson(Fixture f, String quantidade, String inicio, String fim) { return "{\"setorId\":" + f.setor() + ",\"funcionarioId\":" + f.funcionario() + ",\"pecaId\":" + f.peca() + ",\"quantidade\":" + quantidade + ",\"inicio\":\"" + inicio + "\",\"fim\":\"" + fim + "\",\"observacao\":\"Teste\"}"; }
    private record Fixture(long empresa, long setor, long funcionario, long peca) { }
}
