package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.domain.Empresa;
import com.metalurgica.sistemagestaoindustrial.domain.Setor;
import com.metalurgica.sistemagestaoindustrial.domain.Usuario;
import com.metalurgica.sistemagestaoindustrial.repository.EmpresaRepository;
import com.metalurgica.sistemagestaoindustrial.repository.PerfilRepository;
import com.metalurgica.sistemagestaoindustrial.repository.SetorRepository;
import com.metalurgica.sistemagestaoindustrial.repository.UsuarioSetorRepository;
import com.metalurgica.sistemagestaoindustrial.security.JwtTokenService;
import com.metalurgica.sistemagestaoindustrial.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import io.micrometer.core.instrument.MeterRegistry;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RelatorioExecutivoIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired EmpresaRepository empresaRepository;
    @Autowired PerfilRepository perfilRepository;
    @Autowired UsuarioService usuarioService;
    @Autowired JwtTokenService tokenService;
    @Autowired SetorRepository setorRepository;
    @Autowired UsuarioSetorRepository usuarioSetorRepository;
    @Autowired MeterRegistry meterRegistry;

    private Empresa empresa;
    private String adminToken;
    private String gestorToken;
    private Setor permittedSector;
    private Setor forbiddenSector;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setRazaoSocial("Empresa Relatorio");
        empresa.setNomeFantasia("Empresa Relatorio");
        empresa.setCnpj(String.format("%014d", Math.abs(System.nanoTime() % 100000000000000L)));
        empresa.touchForCreate();
        empresa = empresaRepository.save(empresa);
        var admin = usuarioService.create(empresa, perfilRepository.findByCodigo("ADMINISTRADOR").orElseThrow(), "Admin Relatorio", "report-admin-" + empresa.getId() + "@example.test", "SenhaSegura123");
        var gestor = usuarioService.create(empresa, perfilRepository.findByCodigo("GESTOR").orElseThrow(), "Gestor Relatorio", "report-gestor-" + empresa.getId() + "@example.test", "SenhaSegura123");
        permittedSector = sector("PINT", "Pintura");
        forbiddenSector = sector("LASER", "Laser");
        var relation = new com.metalurgica.sistemagestaoindustrial.domain.UsuarioSetor();
        relation.setUsuario(gestor); relation.setSetor(permittedSector); usuarioSetorRepository.save(relation);
        adminToken = tokenService.issue(admin);
        gestorToken = tokenService.issue(gestor);
    }

    @Test
    void returnsCompactExecutiveReportAndHidesFinancialSectionForGestor() throws Exception {
        double jsonBefore = reportCount("json", "success");
        double csvBefore = reportCount("csv", "success");
        double xlsxBefore = reportCount("xlsx", "success");
        var path = "/api/v1/empresas/" + empresa.getId() + "/relatorios/executivo?inicio=2026-09-01T00:00:00&fim=2026-09-30T23:59:00";
        mvc.perform(get(path).header("Authorization", "Bearer " + gestorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.identificacao.empresaId").value(empresa.getId()))
            .andExpect(jsonPath("$.producao.quantidadeProduzida").value(0))
            .andExpect(jsonPath("$.financeiro").doesNotExist());
        var base = "/api/v1/empresas/" + empresa.getId() + "/relatorios";
        mvc.perform(get(base + "/producao?inicio=2026-09-01T00:00:00&fim=2026-09-30T23:59:00").header("Authorization", "Bearer " + gestorToken))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resumo.quantidadeProduzida").value(0));
        mvc.perform(get(base + "/paradas?inicio=2026-09-01T00:00:00&fim=2026-09-30T23:59:00").header("Authorization", "Bearer " + gestorToken))
            .andExpect(status().isOk()).andExpect(jsonPath("$.quantidadeOcorrencias").value(0));
        mvc.perform(get(base + "/desempenho?inicio=2026-09-01T00:00:00&fim=2026-09-30T23:59:00").header("Authorization", "Bearer " + gestorToken))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resumo.cobertura.registrosTotais").value(0));
        mvc.perform(get(base + "/executivo/exportar?inicio=2026-09-01T00:00:00&fim=2026-09-30T23:59:00&formato=CSV").header("Authorization", "Bearer " + gestorToken))
            .andExpect(status().isOk()).andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("text/csv")))
            .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("relatorio-executivo-")));
        mvc.perform(get(base + "/executivo/exportar?inicio=2026-09-01T00:00:00&fim=2026-09-30T23:59:00&formato=XLSX").header("Authorization", "Bearer " + gestorToken))
            .andExpect(status().isOk()).andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("spreadsheetml.sheet")));
        mvc.perform(get(path + "&setorId=" + permittedSector.getId()).header("Authorization", "Bearer " + gestorToken))
            .andExpect(status().isOk());
        mvc.perform(get(path + "&setorId=" + forbiddenSector.getId()).header("Authorization", "Bearer " + gestorToken))
            .andExpect(status().isForbidden());
        org.junit.jupiter.api.Assertions.assertTrue(reportCount("json", "success") >= jsonBefore + 2);
        org.junit.jupiter.api.Assertions.assertEquals(csvBefore + 1, reportCount("csv", "success"));
        org.junit.jupiter.api.Assertions.assertEquals(xlsxBefore + 1, reportCount("xlsx", "success"));
    }

    private double reportCount(String format, String outcome) {
        var counter = meterRegistry.find("report.generation").tags("format", format, "outcome", outcome).counter();
        return counter == null ? 0 : counter.count();
    }

    private Setor sector(String code, String name) {
        var sector = new Setor(); sector.setEmpresa(empresa); sector.setCodigo(code); sector.setNome(name); sector.setNomeNormalizado(name.toUpperCase()); sector.touchForCreate(); return setorRepository.save(sector);
    }

    @Test
    void rejectsMissingTokenAndForeignCompany() throws Exception {
        var path = "/api/v1/empresas/" + empresa.getId() + "/relatorios/executivo?inicio=2026-09-01T00:00:00&fim=2026-09-30T23:59:00";
        mvc.perform(get(path)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/empresas/999999/relatorios/executivo?inicio=2026-09-01T00:00:00&fim=2026-09-30T23:59:00")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isForbidden());
    }
}
