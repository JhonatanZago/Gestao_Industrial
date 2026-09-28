package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.audit.*;
import com.metalurgica.sistemagestaoindustrial.domain.Empresa;
import com.metalurgica.sistemagestaoindustrial.domain.Setor;
import com.metalurgica.sistemagestaoindustrial.domain.UsuarioSetor;
import com.metalurgica.sistemagestaoindustrial.repository.SetorRepository;
import com.metalurgica.sistemagestaoindustrial.repository.UsuarioSetorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:auditoria_security;MODE=PostgreSQL;DB_CLOSE_DELAY=0;DATABASE_TO_LOWER=TRUE")
class AuditoriaSecurityIntegrationTests extends AuthenticatedIntegrationTest {
    @org.springframework.beans.factory.annotation.Autowired EventoAuditoriaRepository eventoRepository;
    @org.springframework.beans.factory.annotation.Autowired SetorRepository setorRepository;
    @org.springframework.beans.factory.annotation.Autowired UsuarioSetorRepository usuarioSetorRepository;
    @org.springframework.beans.factory.annotation.Autowired AuditoriaService auditoriaService;

    private Empresa empresaA;
    private Empresa empresaB;
    private String adminAToken;
    private String gestorAToken;
    private Long pinturaId;
    private Long laserEventId;
    private Long foreignEventId;

    @BeforeEach
    void setUp() {
        usuarioSetorRepository.deleteAll();
        empresaA = empresa("Empresa Auditoria A");
        empresaB = empresa("Empresa Auditoria B");
        var admin = usuarioService.create(empresaA, perfilRepository.findByCodigo("ADMINISTRADOR").orElseThrow(),
                "Admin Auditoria", "audit-admin-" + empresaA.getId() + "@example.test", "SenhaSegura123");
        var gestor = usuarioService.create(empresaA, perfilRepository.findByCodigo("GESTOR").orElseThrow(),
                "Gestor Auditoria", "audit-gestor-" + empresaA.getId() + "@example.test", "SenhaSegura123");
        var pintura = setor(empresaA, "PINT", "Pintura");
        pinturaId = pintura.getId();
        setor(empresaA, "LASER", "Laser");
        var vinculo = new UsuarioSetor();
        vinculo.setUsuario(gestor);
        vinculo.setSetor(pintura);
        usuarioSetorRepository.save(vinculo);
        adminAToken = tokenService.issue(admin);
        gestorAToken = tokenService.issue(gestor);

        eventoRepository.deleteAll();
        eventoRepository.save(new EventoAuditoria(empresaA.getId(), null, null,
                TipoRecursoAuditoria.REGISTRO_PRODUCAO, 101L, AcaoAuditoria.CRIACAO,
                OrigemAuditoria.API, OffsetDateTime.now(), null, "{\"quantidade\":10}",
                "{\"setorId\":" + pinturaId + "}", "corr-a"));
        var laser = eventoRepository.save(new EventoAuditoria(empresaA.getId(), null, null,
                TipoRecursoAuditoria.REGISTRO_PRODUCAO, 102L, AcaoAuditoria.CRIACAO,
                OrigemAuditoria.API, OffsetDateTime.now(), null, "{\"quantidade\":20}",
                "{\"setorId\":999999}", "corr-laser"));
        laserEventId = laser.getId();
        var foreign = eventoRepository.save(new EventoAuditoria(empresaB.getId(), null, null,
                TipoRecursoAuditoria.REGISTRO_PRODUCAO, 201L, AcaoAuditoria.CRIACAO,
                OrigemAuditoria.API, OffsetDateTime.now(), null, "{\"quantidade\":30}",
                "{\"setorId\":1}", "corr-b"));
        foreignEventId = foreign.getId();
    }

    @AfterEach
    void cleanSectorLinks() {
        usuarioSetorRepository.deleteAll();
    }

    @Test
    void bloqueiaEmpresaEstranhaEEventoDeOutraEmpresa() throws Exception {
        mvc.perform(get("/api/v1/empresas/" + empresaB.getId() + "/auditoria")
                        .header("Authorization", "Bearer " + adminAToken))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/empresas/" + empresaA.getId() + "/auditoria/eventos/" + foreignEventId)
                        .header("Authorization", "Bearer " + adminAToken))
                .andExpect(status().is4xxClientError());
        mvc.perform(get("/api/v1/empresas/" + empresaA.getId() + "/auditoria/eventos/" + laserEventId)
                        .header("Authorization", "Bearer " + adminAToken))
                .andExpect(status().isOk());
    }

    @Test
    void aplicaEscopoSetorialEPermissoesSensivel() throws Exception {
        mvc.perform(get("/api/v1/empresas/" + empresaA.getId() + "/auditoria?tipoRecurso=REGISTRO_PRODUCAO")
                        .header("Authorization", "Bearer " + gestorAToken))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.content.length()").value(1));
        mvc.perform(get("/api/v1/empresas/" + empresaA.getId() + "/auditoria?tipoRecurso=VALOR_PRODUCAO")
                        .header("Authorization", "Bearer " + gestorAToken))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/empresas/" + empresaA.getId() + "/auditoria?tipoRecurso=USUARIO")
                        .header("Authorization", "Bearer " + gestorAToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void naoExponeRotasDeMutacaoDeAuditoria() throws Exception {
        mvc.perform(patch("/api/v1/empresas/" + empresaA.getId() + "/auditoria/REGISTRO_PRODUCAO/101")
                        .header("Authorization", "Bearer " + adminAToken))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void naoPersisteNemRetornaTermosSensivelEmEventoDeUsuario() throws Exception {
        var event = auditoriaService.record(empresaA.getId(), TipoRecursoAuditoria.USUARIO, 700L,
                AcaoAuditoria.ALTERACAO_PERMISSAO, OrigemAuditoria.SISTEMA,
                Map.of("senha", "x", "senhaHash", "hash", "token", "jwt", "secret", "s", "nome", "Operador"),
                Map.of("authorization", "Bearer segredo", "credential", "cred", "nome", "Operador"), null, null);
        org.junit.jupiter.api.Assertions.assertFalse(eventoRepository.findById(event.getId()).orElseThrow().getDadosNovos().contains("token"));
        mvc.perform(get("/api/v1/empresas/" + empresaA.getId() + "/auditoria/eventos/" + event.getId())
                        .header("Authorization", "Bearer " + adminAToken))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.containsString("password"), org.hamcrest.Matchers.containsString("senha"),
                        org.hamcrest.Matchers.containsString("token"), org.hamcrest.Matchers.containsString("secret"),
                        org.hamcrest.Matchers.containsString("authorization"), org.hamcrest.Matchers.containsString("credential")))));
    }

    @Test
    void auditaCriacaoEAlteracaoDePerfilComAtorEAlvo() throws Exception {
        var target = usuarioService.create(empresaA, perfilRepository.findByCodigo("GESTOR").orElseThrow(),
                "Usuário Alvo", "audit-target-" + empresaA.getId() + "@example.test", "SenhaSegura123");
        var creation = eventoRepository.findAll().stream()
                .filter(e -> e.getTipoRecurso() == TipoRecursoAuditoria.USUARIO && e.getRecursoId().equals(target.getId()))
                .findFirst().orElseThrow();
        org.junit.jupiter.api.Assertions.assertFalse(String.valueOf(creation.getDadosNovos()).contains("senha"));

        mvc.perform(patch("/api/v1/empresas/" + empresaA.getId() + "/usuarios/" + target.getId() + "/perfil")
                        .header("Authorization", "Bearer " + adminAToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"perfil\":\"GESTOR\"}"))
                .andExpect(status().isOk());
        var permissionEvent = eventoRepository.findAll().stream()
                .filter(e -> e.getTipoRecurso() == TipoRecursoAuditoria.PERFIL && e.getRecursoId().equals(target.getId()))
                .findFirst().orElseThrow();
        org.junit.jupiter.api.Assertions.assertNotNull(permissionEvent.getUsuarioId());
        org.junit.jupiter.api.Assertions.assertEquals(AcaoAuditoria.ALTERACAO_PERMISSAO, permissionEvent.getAcao());
    }

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
