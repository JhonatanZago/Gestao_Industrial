package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.domain.*;
import com.metalurgica.sistemagestaoindustrial.audit.EventoAuditoriaRepository;
import com.metalurgica.sistemagestaoindustrial.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QualidadeDadosIntegrationTests extends AuthenticatedIntegrationTest {
    @Autowired SetorRepository setorRepository;
    @Autowired FuncionarioRepository funcionarioRepository;
    @Autowired PecaRepository pecaRepository;
    @Autowired RegistroProducaoRepository producaoRepository;
    @Autowired ParadaProducaoRepository paradaRepository;
    @Autowired MotivoParadaRepository motivoRepository;
    @Autowired ImportacaoProducaoRepository importacaoRepository;
    @Autowired ResultadoLinhaImportacaoRepository resultadoImportacaoRepository;
    @Autowired TempoPadraoProducaoRepository tempoRepository;
    @Autowired ValorProducaoRepository valorRepository;
    @Autowired EventoAuditoriaRepository eventoAuditoriaRepository;

    @BeforeEach
    void clean() {
        resultadoImportacaoRepository.deleteAll(); importacaoRepository.deleteAll(); paradaRepository.deleteAll(); motivoRepository.deleteAll(); eventoAuditoriaRepository.deleteAll();
        producaoRepository.deleteAll(); tempoRepository.deleteAll(); valorRepository.deleteAll(); usuarioRepository.deleteAll(); pecaRepository.deleteAll(); funcionarioRepository.deleteAll(); setorRepository.deleteAll(); empresaRepository.deleteAll();
    }

    @Test
    void exponeCoberturaAusenteDuplicidadeEFiltrosSemPersistirDiagnostico() throws Exception {
        var fixture = fixture();
        var first = production(fixture, "100");
        var duplicate = productionEntity(fixture, "100", first);
        producaoRepository.save(duplicate);
        var period = "inicio=2026-09-26T00:00:00&fim=2026-09-26T23:59:59";

        mvc.perform(get("/api/v1/empresas/" + fixture.empresa + "/qualidade-dados/resumo?" + period))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coberturaTempoPadrao.registrosTotais").value(2))
                .andExpect(jsonPath("$.coberturaTempoPadrao.registrosCobertos").value(0))
                .andExpect(jsonPath("$.coberturaValor.registrosDescobertos").value(2))
                .andExpect(jsonPath("$.status").value("BAIXA"));
        mvc.perform(get("/api/v1/empresas/" + fixture.empresa + "/qualidade-dados/duplicidades?" + period))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].registrosRelacionados.length()").value(2));
        mvc.perform(get("/api/v1/empresas/" + fixture.empresa + "/qualidade-dados/problemas?" + period + "&codigo=QD-DUP-001"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/api/v1/empresas/" + fixture.empresa + "/analytics/resumo?" + period))
                .andExpect(status().isOk()).andExpect(jsonPath("$.qualidadeDados").doesNotExist());
        mvc.perform(get("/api/v1/empresas/" + fixture.empresa + "/analytics/resumo?" + period + "&incluirQualidade=true"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.qualidadeDados.coberturaTempoPadrao.registrosTotais").value(2));
        mvc.perform(get("/api/v1/empresas/" + fixture.empresa + "/qualidade-dados/problemas?inicio=2026-09-26T00:00:00&fim=2028-09-26T00:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaEmpresaForaDoTokenEnaoExpoeRotaSemAutenticacao() throws Exception {
        var fixture = fixture();
        rawMvc.perform(get("/api/v1/empresas/" + fixture.empresa + "/qualidade-dados/resumo?inicio=2026-09-26T00:00:00&fim=2026-09-26T23:59:59"))
                .andExpect(status().isUnauthorized());
        rawMvc.perform(get("/api/v1/empresas/999999/qualidade-dados/resumo?inicio=2026-09-26T00:00:00&fim=2026-09-26T23:59:59"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void calculaCenarioDeterministicoDeCoberturaOitentaENoventaPorCento() throws Exception {
        var f = fixture();
        mvc.perform(post("/api/v1/empresas/" + f.empresa + "/pecas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"90000\",\"descricao\":\"Outra peca\"}"))
                .andExpect(status().isCreated());
        var otherPart = pecaRepository.findByEmpresaIdAndCodigo(f.empresa, "90000").orElseThrow();
        var first = production(f, "100");
        var source = producaoRepository.findById(first).orElseThrow();
        for (int i = 1; i < 4; i++) {
            var copy = productionEntity(f, "100", first); copy.setInicio(LocalDateTime.of(2026, 9, 26, 9 + i, 0)); copy.setFim(LocalDateTime.of(2026, 9, 26, 10 + i, 0)); producaoRepository.save(copy);
        }
        var uncovered = productionEntity(f, "100", first); uncovered.setPeca(otherPart); uncovered.setInicio(LocalDateTime.of(2026, 9, 26, 14, 0)); uncovered.setFim(LocalDateTime.of(2026, 9, 26, 15, 0)); producaoRepository.save(uncovered);
        var standard = new TempoPadraoProducao(); standard.setEmpresa(source.getEmpresa()); standard.setSetor(source.getSetor()); standard.setPeca(source.getPeca()); standard.setSegundos(new BigDecimal("60")); standard.setBaseTempo(BaseTempoPadrao.POR_UNIDADE); standard.setVigenciaInicio(java.time.LocalDate.of(2026, 1, 1)); standard.touchForCreate(); tempoRepository.save(standard);
        var value = new ValorProducao(); value.setEmpresa(source.getEmpresa()); value.setSetor(source.getSetor()); value.setPeca(source.getPeca()); value.setTipoValor(TipoValor.VALOR_PRODUCAO); value.setValorUnitario(new BigDecimal("2.00")); value.setMoeda("BRL"); value.setVigenciaInicio(java.time.LocalDate.of(2026, 1, 1)); value.touchForCreate(); valorRepository.save(value);
        mvc.perform(get("/api/v1/empresas/" + f.empresa + "/qualidade-dados/resumo?inicio=2026-09-26T00:00:00&fim=2026-09-26T23:59:59"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.coberturaTempoPadrao.registrosTotais").value(5))
                .andExpect(jsonPath("$.coberturaTempoPadrao.registrosCobertos").value(4)).andExpect(jsonPath("$.coberturaTempoPadrao.percentualCobertura").value(80))
                .andExpect(jsonPath("$.coberturaValor.registrosCobertos").value(4)).andExpect(jsonPath("$.coberturaValor.percentualCobertura").value(80));
    }

    @Test
    void diagnosticaDadosLegadosSemAlterarRegistrosNemAuditoria() throws Exception {
        var f = fixture();
        var productionId = production(f, "100");
        mvc.perform(post("/api/v1/empresas/" + f.empresa + "/motivos-parada").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"MAT-001\",\"nome\":\"Material\",\"categoria\":\"OPERACIONAL\"}"))
                .andExpect(status().isCreated());
        var motivo = motivoRepository.findByEmpresaIdAndCodigo(f.empresa, "MAT-001").orElseThrow();
        var production = producaoRepository.findById(productionId).orElseThrow();
        var stop = new ParadaProducao(); stop.setEmpresa(production.getEmpresa()); stop.setRegistroProducao(production); stop.setMotivoParada(motivo);
        stop.setInicio(LocalDateTime.of(2026, 9, 26, 8, 0)); stop.setFim(LocalDateTime.of(2026, 9, 26, 9, 20)); stop.touchForCreate(); paradaRepository.save(stop);
        var before = eventoAuditoriaRepository.count();
        mvc.perform(get("/api/v1/empresas/" + f.empresa + "/qualidade-dados/problemas?inicio=2026-09-26T00:00:00&fim=2026-09-26T23:59:59&codigo=QD-PARADA-002"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/api/v1/empresas/" + f.empresa + "/qualidade-dados/resumo?inicio=2026-09-26T00:00:00&fim=2026-09-26T23:59:59"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.coberturaValor.valorUnitario").doesNotExist());
        org.junit.jupiter.api.Assertions.assertEquals(before, eventoAuditoriaRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(1, paradaRepository.count());
    }

    @Test
    void diagnosticaIntervalosQuantidadeEConflitoDeVigenciaLegados() throws Exception {
        var f = fixture();
        var productionId = production(f, "100000");
        var source = producaoRepository.findById(productionId).orElseThrow();
        var invalid = productionEntity(f, "1", productionId); invalid.setInicio(LocalDateTime.of(2026, 9, 26, 11, 0)); invalid.setFim(LocalDateTime.of(2026, 9, 28, 10, 0)); producaoRepository.save(invalid);
        var first = new TempoPadraoProducao(); first.setEmpresa(source.getEmpresa()); first.setSetor(source.getSetor()); first.setPeca(source.getPeca()); first.setSegundos(new BigDecimal("60")); first.setBaseTempo(BaseTempoPadrao.POR_UNIDADE); first.setVigenciaInicio(java.time.LocalDate.of(2026, 1, 1)); first.setVigenciaFim(java.time.LocalDate.of(2026, 12, 31)); first.touchForCreate(); tempoRepository.save(first);
        var second = new TempoPadraoProducao(); second.setEmpresa(source.getEmpresa()); second.setSetor(source.getSetor()); second.setPeca(source.getPeca()); second.setSegundos(new BigDecimal("90")); second.setBaseTempo(BaseTempoPadrao.POR_UNIDADE); second.setVigenciaInicio(java.time.LocalDate.of(2026, 6, 1)); second.touchForCreate(); tempoRepository.save(second);
        var period = "inicio=2026-09-26T00:00:00&fim=2026-09-26T23:59:59";
        mvc.perform(get("/api/v1/empresas/" + f.empresa + "/qualidade-dados/problemas?" + period + "&codigo=QD-TEMP-002"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/api/v1/empresas/" + f.empresa + "/qualidade-dados/problemas?" + period + "&codigo=QD-QTD-001"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/api/v1/empresas/" + f.empresa + "/qualidade-dados/problemas?" + period + "&codigo=QD-VIG-001"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
    }

    private long production(Fixture f, String quantity) throws Exception {
        mvc.perform(post("/api/v1/empresas/" + f.empresa + "/producoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"setorId\":" + f.setor + ",\"funcionarioId\":" + f.funcionario + ",\"pecaId\":" + f.peca + ",\"quantidade\":" + quantity + ",\"inicio\":\"2026-09-26T08:00:00\",\"fim\":\"2026-09-26T09:00:00\"}"))
                .andExpect(status().isCreated());
        return producaoRepository.findAll().stream().max(java.util.Comparator.comparing(RegistroProducao::getId)).orElseThrow().getId();
    }

    private RegistroProducao productionEntity(Fixture f, String quantity, long sourceId) {
        var source = producaoRepository.findById(sourceId).orElseThrow();
        var value = new RegistroProducao(); value.setEmpresa(source.getEmpresa()); value.setSetor(source.getSetor()); value.setFuncionario(source.getFuncionario());
        value.setPeca(source.getPeca()); value.setQuantidade(new BigDecimal(quantity)); value.setInicio(source.getInicio()); value.setFim(source.getFim()); value.touchForCreate();
        return value;
    }

    private Fixture fixture() throws Exception {
        mvc.perform(post("/api/v1/empresas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"razaoSocial\":\"Qualidade\",\"nomeFantasia\":\"Qualidade\",\"cnpj\":\"47777777777777\"}"))
                .andExpect(status().isCreated());
        long empresa = empresaRepository.findByCnpj("47777777777777").orElseThrow().getId();
        mvc.perform(post("/api/v1/empresas/" + empresa + "/setores").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"PINT\",\"nome\":\"Pintura\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/empresas/" + empresa + "/funcionarios").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"00147\",\"nome\":\"Operador\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/empresas/" + empresa + "/pecas").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"54871\",\"descricao\":\"Suporte\"}"))
                .andExpect(status().isCreated());
        return new Fixture(empresa, setorRepository.findByEmpresaIdAndCodigo(empresa, "PINT").orElseThrow().getId(),
                funcionarioRepository.findByEmpresaIdAndCodigo(empresa, "00147").orElseThrow().getId(), pecaRepository.findByEmpresaIdAndCodigo(empresa, "54871").orElseThrow().getId());
    }

    private record Fixture(long empresa, long setor, long funcionario, long peca) { }
}
