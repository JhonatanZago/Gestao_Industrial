package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.domain.Empresa;
import com.metalurgica.sistemagestaoindustrial.domain.Perfil;
import com.metalurgica.sistemagestaoindustrial.domain.Setor;
import com.metalurgica.sistemagestaoindustrial.domain.UsuarioSetor;
import com.metalurgica.sistemagestaoindustrial.repository.EmpresaRepository;
import com.metalurgica.sistemagestaoindustrial.repository.PerfilRepository;
import com.metalurgica.sistemagestaoindustrial.repository.SetorRepository;
import com.metalurgica.sistemagestaoindustrial.repository.UsuarioRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MultiEmpresaEscopoIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired EmpresaRepository empresaRepository;
    @Autowired PerfilRepository perfilRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired UsuarioService usuarioService;
    @Autowired JwtTokenService tokenService;
    @Autowired SetorRepository setorRepository;
    @Autowired UsuarioSetorRepository usuarioSetorRepository;

    private Empresa empresaA;
    private Empresa empresaB;
    private String adminAToken;
    private String gestorToken;
    private Setor setorPermitido;
    private Setor setorProibido;

    @BeforeEach
    void setUp() {
        empresaA = empresa("Empresa A");
        empresaB = empresa("Empresa B");
        var suffix = System.nanoTime();
        var admin = usuarioService.create(empresaA, perfil("ADMINISTRADOR"), "Admin A", "multi-admin-a-" + suffix + "@example.test", "SenhaSegura123");
        adminAToken = tokenService.issue(admin);

        setorPermitido = setor(empresaA, "PINT", "Pintura");
        setorProibido = setor(empresaA, "LASER", "Laser");
        var gestor = usuarioService.create(empresaA, perfil("GESTOR"), "Gestor A", "multi-gestor-a-" + suffix + "@example.test", "SenhaSegura123");
        var vinculo = new UsuarioSetor();
        vinculo.setUsuario(gestor);
        vinculo.setSetor(setorPermitido);
        usuarioSetorRepository.save(vinculo);
        gestorToken = tokenService.issue(gestor);
    }

    @Test
    void adminDeEmpresaNaoAcessaRecursosDaOutraEmpresa() throws Exception {
        String[] paths = {
            "/funcionarios",
            "/producoes",
            "/analytics/resumo?inicio=2026-09-26T00:00:00&fim=2026-09-26T23:59:59",
            "/qualidade-dados/resumo?inicio=2026-09-26T00:00:00&fim=2026-09-26T23:59:59",
            "/analytics/financeiro/resumo",
            "/metas-producao",
            "/importacoes/producao"
        };
        for (String path : paths) {
            mvc.perform(get("/api/v1/empresas/" + empresaB.getId() + path)
                    .header("Authorization", "Bearer " + adminAToken))
                .andExpect(status().isForbidden());
        }
    }

    @Test
    void gestorRecebeApenasSetorVinculadoEConsultaExplicitaForaDoEscopoEhNegada() throws Exception {
        String base = "/api/v1/empresas/" + empresaA.getId() + "/analytics/resumo";
        mvc.perform(get(base + "?inicio=2026-09-26T00:00:00&fim=2026-09-26T23:59:59&setorId=" + setorPermitido.getId())
                .header("Authorization", "Bearer " + gestorToken))
            .andExpect(status().isOk());
        mvc.perform(get(base + "?inicio=2026-09-26T00:00:00&fim=2026-09-26T23:59:59&setorId=" + setorProibido.getId())
            .header("Authorization", "Bearer " + gestorToken))
            .andExpect(status().isForbidden());
        String quality = "/api/v1/empresas/" + empresaA.getId() + "/qualidade-dados/resumo";
        mvc.perform(get(quality + "?inicio=2026-09-26T00:00:00&fim=2026-09-26T23:59:59&setorId=" + setorPermitido.getId())
                .header("Authorization", "Bearer " + gestorToken)).andExpect(status().isOk());
        mvc.perform(get(quality + "?inicio=2026-09-26T00:00:00&fim=2026-09-26T23:59:59&setorId=" + setorProibido.getId())
                .header("Authorization", "Bearer " + gestorToken)).andExpect(status().isForbidden());
    }

    private Perfil perfil(String codigo) { return perfilRepository.findByCodigo(codigo).orElseThrow(); }

    private Empresa empresa(String nome) {
        var empresa = new Empresa();
        empresa.setRazaoSocial(nome);
        empresa.setNomeFantasia(nome);
        empresa.setCnpj(String.format("%014d", Math.abs(System.nanoTime() % 100000000000000L)));
        empresa.touchForCreate();
        return empresaRepository.save(empresa);
    }

    private Setor setor(Empresa empresa, String codigo, String nome) {
        var setor = new Setor();
        setor.setEmpresa(empresa);
        setor.setCodigo(codigo);
        setor.setNome(nome);
        setor.setNomeNormalizado(nome.toUpperCase());
        setor.touchForCreate();
        return setorRepository.save(setor);
    }
}
