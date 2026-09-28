package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnalyticsIntegrationTests extends AuthenticatedIntegrationTest {
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

    @BeforeEach
    void clean() { resultadoImportacaoRepository.deleteAll(); importacaoRepository.deleteAll(); paradaRepository.deleteAll(); motivoRepository.deleteAll(); producaoRepository.deleteAll(); maquinaRepository.deleteAll(); turnoRepository.deleteAll(); pecaRepository.deleteAll(); funcionarioRepository.deleteAll(); setorRepository.deleteAll(); usuarioRepository.deleteAll(); empresaRepository.deleteAll(); }

    @Test
    void calculaResumoAgrupamentoSerieEComparativo() throws Exception {
        var f = fixture();
        var motivo = createMotivo(f.empresa());
        var first = createProduction(f, "300", "2026-09-26T08:00:00", "2026-09-26T10:00:00");
        var second = createProduction(f, "200", "2026-09-26T10:00:00", "2026-09-26T12:00:00");
        createStop(f, first, motivo, "2026-09-26T08:20:00", "2026-09-26T08:40:00");
        createStop(f, second, motivo, "2026-09-26T10:30:00", "2026-09-26T10:40:00");
        var period = "inicio=2026-09-26T00:00:00&fim=2026-09-26T23:59:59";

        mvc.perform(get("/api/v1/empresas/" + f.empresa() + "/analytics/resumo?" + period))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantidadeProduzida").value(500))
                .andExpect(jsonPath("$.quantidadeRegistros").value(2)).andExpect(jsonPath("$.duracaoBrutaMinutos").value(240))
                .andExpect(jsonPath("$.tempoParadoMinutos").value(30)).andExpect(jsonPath("$.tempoProdutivoMinutos").value(210))
                .andExpect(jsonPath("$.taxaProducaoBruta").value(125)).andExpect(jsonPath("$.taxaProducaoProdutiva").value(142.86))
                .andExpect(jsonPath("$.percentualTempoParado").value(12.5));
        mvc.perform(get("/api/v1/empresas/" + f.empresa() + "/analytics/setores?" + period))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].quantidadeProduzida").value(500));
        mvc.perform(get("/api/v1/empresas/" + f.empresa() + "/analytics/paradas/motivos?" + period))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].quantidadeOcorrencias").value(2)).andExpect(jsonPath("$.content[0].tempoParadoMinutos").value(30));
        mvc.perform(get("/api/v1/empresas/" + f.empresa() + "/analytics/serie-temporal?" + period + "&granularidade=DIA"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].quantidadeProduzida").value(500));
        mvc.perform(get("/api/v1/empresas/" + f.empresa() + "/analytics/serie-temporal?" + period + "&granularidade=SEMANA"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].quantidadeProduzida").value(500));
        mvc.perform(get("/api/v1/empresas/" + f.empresa() + "/analytics/serie-temporal?" + period + "&granularidade=MES"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].quantidadeProduzida").value(500));
        mvc.perform(get("/api/v1/empresas/" + f.empresa() + "/analytics/serie-temporal?" + period + "&granularidade=HORA"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Parâmetro de consulta inválido ou ausente."));
        mvc.perform(get("/api/v1/empresas/" + f.empresa() + "/analytics/maquinas?" + period))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].codigo").value("SEM_MAQUINA"));
        mvc.perform(get("/api/v1/empresas/" + f.empresa() + "/analytics/turnos?" + period))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].codigo").value("SEM_TURNO"));
        mvc.perform(get("/api/v1/empresas/" + f.empresa() + "/analytics/funcionarios/" + f.funcionario() + "?" + period))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantidadeProduzida").value(500)).andExpect(jsonPath("$.pecas[0].codigo").value("54871"));
        mvc.perform(get("/api/v1/empresas/" + f.empresa() + "/analytics/comparativo?inicioAtual=2026-09-26T00:00:00&fimAtual=2026-09-26T23:59:59&inicioAnterior=2026-09-25T00:00:00&fimAnterior=2026-09-25T23:59:59"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantidadeProduzida.valorAtual").value(500)).andExpect(jsonPath("$.quantidadeProduzida.variacaoPercentual").doesNotExist());
    }

    @Test
    void rejeitaPeriodoInvalido() throws Exception {
        var f = fixture();
        mvc.perform(get("/api/v1/empresas/" + f.empresa() + "/analytics/resumo?inicio=2026-09-27T00:00:00&fim=2026-09-26T00:00:00"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("O fim do período analítico deve ser posterior ou igual ao início."));
    }

    @Test
    void usaInicioComoRegraDePeriodoEPreservaHistoricoDeDimensaoInativa() throws Exception {
        var f = fixture();
        createProduction(f, "100", "2026-09-26T07:30:00", "2026-09-26T08:30:00");
        var included = createProduction(f, "200", "2026-09-26T09:00:00", "2026-09-26T10:00:00");
        var period = "inicio=2026-09-26T08:00:00&fim=2026-09-26T10:00:00";
        mvc.perform(get("/api/v1/empresas/" + f.empresa() + "/analytics/resumo?" + period)).andExpect(status().isOk()).andExpect(jsonPath("$.quantidadeProduzida").value(200));
        var employee = funcionarioRepository.findById(f.funcionario()).orElseThrow(); employee.setAtivo(false); funcionarioRepository.save(employee);
        mvc.perform(get("/api/v1/empresas/" + f.empresa() + "/analytics/resumo?" + period)).andExpect(status().isOk()).andExpect(jsonPath("$.quantidadeProduzida").value(200));
        var production = producaoRepository.findById(included).orElseThrow(); production.setAtivo(false); producaoRepository.save(production);
        mvc.perform(get("/api/v1/empresas/" + f.empresa() + "/analytics/resumo?" + period)).andExpect(status().isOk()).andExpect(jsonPath("$.quantidadeProduzida").value(0)).andExpect(jsonPath("$.taxaProducaoBruta").doesNotExist());
    }

    private long createMotivo(long empresa) throws Exception {
        mvc.perform(post("/api/v1/empresas/" + empresa + "/motivos-parada").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"MAT-001\",\"nome\":\"Material\",\"categoria\":\"OPERACIONAL\"}")).andExpect(status().isCreated());
        return motivoRepository.findByEmpresaIdAndCodigo(empresa, "MAT-001").orElseThrow().getId();
    }

    @Test
    void isolaEmpresasSetoresDimensoesERecursosOpcionais() throws Exception {
        var a = fixture();
        var b = otherFixture();
        mvc.perform(post("/api/v1/empresas/" + a.empresa() + "/setores").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"SOLDA\",\"nome\":\"Solda\"}")).andExpect(status().isCreated());
        long secondSector = setorRepository.findByEmpresaIdAndCodigo(a.empresa(), "SOLDA").orElseThrow().getId();
        mvc.perform(post("/api/v1/empresas/" + a.empresa() + "/funcionarios").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"00231\",\"nome\":\"Auxiliar\"}")).andExpect(status().isCreated());
        long secondEmployee = funcionarioRepository.findByEmpresaIdAndCodigo(a.empresa(), "00231").orElseThrow().getId();
        mvc.perform(post("/api/v1/empresas/" + a.empresa() + "/pecas").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"90000\",\"descricao\":\"Chapa\"}")).andExpect(status().isCreated());
        long secondPart = pecaRepository.findByEmpresaIdAndCodigo(a.empresa(), "90000").orElseThrow().getId();
        mvc.perform(post("/api/v1/empresas/" + a.empresa() + "/maquinas").contentType(MediaType.APPLICATION_JSON).content("{\"setorId\":" + a.setor() + ",\"codigo\":\"M-01\",\"nome\":\"Prensa\"}")).andExpect(status().isCreated());
        long machine = maquinaRepository.findByEmpresaIdAndCodigo(a.empresa(), "M-01").orElseThrow().getId();
        mvc.perform(post("/api/v1/empresas/" + a.empresa() + "/turnos").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"T1\",\"nome\":\"Dia\",\"horaInicio\":\"06:00\",\"horaFim\":\"14:00\"}")).andExpect(status().isCreated());
        long shift = turnoRepository.findByEmpresaIdAndCodigo(a.empresa(), "T1").orElseThrow().getId();
        createProductionWithResources(a, "100", "2026-09-26T08:00:00", "2026-09-26T09:00:00", machine, shift);
        createProduction(new Fixture(a.empresa(), secondSector, secondEmployee, secondPart), "50", "2026-09-26T08:00:00", "2026-09-26T09:00:00");
        createProduction(b, "900", "2026-09-26T08:00:00", "2026-09-26T09:00:00");
        var period = "inicio=2026-09-26T00:00:00&fim=2026-09-26T23:59:59";
        mvc.perform(get("/api/v1/empresas/" + a.empresa() + "/analytics/resumo?" + period)).andExpect(status().isOk()).andExpect(jsonPath("$.quantidadeProduzida").value(150));
        mvc.perform(get("/api/v1/empresas/" + b.empresa() + "/analytics/resumo?" + period)).andExpect(status().isOk()).andExpect(jsonPath("$.quantidadeProduzida").value(900));
        mvc.perform(get("/api/v1/empresas/" + a.empresa() + "/analytics/resumo?" + period + "&setorId=" + b.setor())).andExpect(status().isForbidden());
    }

    private long createProduction(Fixture f, String quantity, String start, String end) throws Exception {
        mvc.perform(post("/api/v1/empresas/" + f.empresa() + "/producoes").contentType(MediaType.APPLICATION_JSON).content("{\"setorId\":" + f.setor() + ",\"funcionarioId\":" + f.funcionario() + ",\"pecaId\":" + f.peca() + ",\"quantidade\":" + quantity + ",\"inicio\":\"" + start + "\",\"fim\":\"" + end + "\"}"))
                .andExpect(status().isCreated());
        return producaoRepository.findAll().stream().max(java.util.Comparator.comparing(com.metalurgica.sistemagestaoindustrial.domain.RegistroProducao::getId)).orElseThrow().getId();
    }

    private long createProductionWithResources(Fixture f, String quantity, String start, String end, long machine, long shift) throws Exception {
        mvc.perform(post("/api/v1/empresas/" + f.empresa() + "/producoes").contentType(MediaType.APPLICATION_JSON).content("{\"setorId\":" + f.setor() + ",\"funcionarioId\":" + f.funcionario() + ",\"pecaId\":" + f.peca() + ",\"maquinaId\":" + machine + ",\"turnoId\":" + shift + ",\"quantidade\":" + quantity + ",\"inicio\":\"" + start + "\",\"fim\":\"" + end + "\"}"))
                .andExpect(status().isCreated());
        return producaoRepository.findAll().stream().filter(p -> p.getEmpresa().getId().equals(f.empresa())).max(java.util.Comparator.comparing(com.metalurgica.sistemagestaoindustrial.domain.RegistroProducao::getId)).orElseThrow().getId();
    }

    private void createStop(Fixture f, long production, long reason, String start, String end) throws Exception {
        mvc.perform(post("/api/v1/empresas/" + f.empresa() + "/producoes/" + production + "/paradas").contentType(MediaType.APPLICATION_JSON).content("{\"motivoParadaId\":" + reason + ",\"inicio\":\"" + start + "\",\"fim\":\"" + end + "\"}"))
                .andExpect(status().isCreated());
    }

    private Fixture fixture() throws Exception {
        mvc.perform(post("/api/v1/empresas").contentType(MediaType.APPLICATION_JSON).content("{\"razaoSocial\":\"Analytics\",\"nomeFantasia\":\"Analytics\",\"cnpj\":\"49999999999999\"}")).andExpect(status().isCreated());
        long company = empresaRepository.findByCnpj("49999999999999").orElseThrow().getId();
        mvc.perform(post("/api/v1/empresas/" + company + "/setores").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"PINT\",\"nome\":\"Pintura\"}")).andExpect(status().isCreated());
        mvc.perform(post("/api/v1/empresas/" + company + "/funcionarios").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"00147\",\"nome\":\"Operador\"}")).andExpect(status().isCreated());
        mvc.perform(post("/api/v1/empresas/" + company + "/pecas").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"54871\",\"descricao\":\"Suporte\"}")).andExpect(status().isCreated());
        return new Fixture(company, setorRepository.findByEmpresaIdAndCodigo(company, "PINT").orElseThrow().getId(), funcionarioRepository.findByEmpresaIdAndCodigo(company, "00147").orElseThrow().getId(), pecaRepository.findByEmpresaIdAndCodigo(company, "54871").orElseThrow().getId());
    }

    private Fixture otherFixture() throws Exception {
        mvc.perform(post("/api/v1/empresas").contentType(MediaType.APPLICATION_JSON).content("{\"razaoSocial\":\"Analytics B\",\"nomeFantasia\":\"Analytics B\",\"cnpj\":\"48888888888888\"}")).andExpect(status().isCreated());
        long company = empresaRepository.findByCnpj("48888888888888").orElseThrow().getId();
        mvc.perform(post("/api/v1/empresas/" + company + "/setores").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"PINT\",\"nome\":\"Pintura\"}")).andExpect(status().isCreated());
        mvc.perform(post("/api/v1/empresas/" + company + "/funcionarios").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"00147\",\"nome\":\"Operador\"}")).andExpect(status().isCreated());
        mvc.perform(post("/api/v1/empresas/" + company + "/pecas").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"54871\",\"descricao\":\"Suporte\"}")).andExpect(status().isCreated());
        return new Fixture(company, setorRepository.findByEmpresaIdAndCodigo(company, "PINT").orElseThrow().getId(), funcionarioRepository.findByEmpresaIdAndCodigo(company, "00147").orElseThrow().getId(), pecaRepository.findByEmpresaIdAndCodigo(company, "54871").orElseThrow().getId());
    }

    private record Fixture(long empresa, long setor, long funcionario, long peca) { }
}
