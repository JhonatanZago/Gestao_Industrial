package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.domain.Empresa;
import com.metalurgica.sistemagestaoindustrial.repository.EmpresaRepository;
import com.metalurgica.sistemagestaoindustrial.repository.PerfilRepository;
import com.metalurgica.sistemagestaoindustrial.repository.UsuarioRepository;
import com.metalurgica.sistemagestaoindustrial.security.JwtTokenService;
import com.metalurgica.sistemagestaoindustrial.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired EmpresaRepository empresaRepository;
    @Autowired PerfilRepository perfilRepository;
    @Autowired UsuarioService usuarioService;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired JwtTokenService tokenService;
    @Autowired JwtDecoder jwtDecoder;

    private String email;
    private String adminEmail;
    private String gestorEmail;

    @BeforeEach
    void setUp() {
        var empresa = new Empresa();
        empresa.setRazaoSocial("Empresa Auth");
        empresa.setNomeFantasia("Empresa Auth");
        empresa.setCnpj(String.format("%014d", Math.abs(System.nanoTime() % 100000000000000L)));
        empresa.touchForCreate();
        empresa = empresaRepository.save(empresa);
        var perfil = perfilRepository.findByCodigo("OPERACIONAL").orElseThrow();
        email = "auth-" + empresa.getId() + "@example.test";
        usuarioService.create(empresa, perfil, "Usuário Auth", email, "SenhaSegura123");
        var admin = perfilRepository.findByCodigo("ADMINISTRADOR").orElseThrow();
        adminEmail = "admin-" + empresa.getId() + "@example.test";
        usuarioService.create(empresa, admin, "Admin Auth", adminEmail, "SenhaSegura123");
        var gestor = perfilRepository.findByCodigo("GESTOR").orElseThrow();
        gestorEmail = "gestor-" + empresa.getId() + "@example.test";
        usuarioService.create(empresa, gestor, "Gestor Auth", gestorEmail, "SenhaSegura123");
    }

    @Test
    void logsInAndReturnsBearerTokenWithoutPasswordData() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"senha\":\"SenhaSegura123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isString())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    @Test
    void usesGenericUnauthorizedResponseForUnknownAndWrongCredentials() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"unknown@example.test\",\"senha\":\"SenhaSegura123\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Credenciais inválidas."));

        mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"senha\":\"errada123\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Credenciais inválidas."));
    }

    @Test
    void signsTokenWithIdentityScopeAndCredentialVersionClaims() {
        var user = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow();
        var token = tokenService.issue(user);
        var jwt = jwtDecoder.decode(token);

        org.junit.jupiter.api.Assertions.assertEquals(user.getId().toString(), jwt.getSubject());
        org.junit.jupiter.api.Assertions.assertEquals(user.getEmpresa().getId(), jwt.getClaim("empresaId"));
        org.junit.jupiter.api.Assertions.assertEquals(user.getPerfil().getCodigo(), jwt.getClaim("perfil"));
        org.junit.jupiter.api.Assertions.assertEquals(user.getCredenciaisVersao(), ((Number) jwt.getClaim("credenciaisVersao")).longValue());
        org.junit.jupiter.api.Assertions.assertNotNull(jwt.getIssuedAt());
        org.junit.jupiter.api.Assertions.assertNotNull(jwt.getExpiresAt());
        org.junit.jupiter.api.Assertions.assertNull(jwt.getClaim("senha"));
        org.junit.jupiter.api.Assertions.assertNull(jwt.getClaim("senhaHash"));
    }

    @Test
    void exposesMeAndInvalidatesPreviousTokenAfterPasswordChange() throws Exception {
        var login = mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"senha\":\"SenhaSegura123\"}"))
            .andReturn().getResponse().getContentAsString();
        var token = login.replaceFirst(".*\\\"accessToken\\\":\\\"([^\\\"]+)\\\".*", "$1");

        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.senhaHash").doesNotExist());

        mvc.perform(patch("/api/v1/auth/senha")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"senhaAtual\":\"SenhaSegura123\",\"novaSenha\":\"NovaSenha123\"}"))
            .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void enforcesUserPermissionAndCompanyScope() throws Exception {
        var operationalToken = loginToken(email);
        mvc.perform(get("/api/v1/empresas/1/usuarios").header("Authorization", "Bearer " + operationalToken))
            .andExpect(status().isForbidden());
        var operationalCompany = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow().getEmpresa().getId();
        mvc.perform(get("/api/v1/empresas/" + operationalCompany + "/analytics/financeiro/resumo")
                .header("Authorization", "Bearer " + operationalToken))
            .andExpect(status().isForbidden());

        var adminToken = loginToken(adminEmail);
        var companyId = usuarioRepository.findByEmailIgnoreCase(adminEmail).orElseThrow().getEmpresa().getId();
        mvc.perform(get("/api/v1/empresas/" + companyId + "/usuarios").header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());
        var gestorToken = loginToken(gestorEmail);
        mvc.perform(get("/api/v1/empresas/" + companyId + "/setores").header("Authorization", "Bearer " + gestorToken))
            .andExpect(status().isOk());
        mvc.perform(post("/api/v1/empresas/" + companyId + "/usuarios")
                .header("Authorization", "Bearer " + gestorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Sem acesso\",\"email\":\"sem-acesso@example.test\",\"senha\":\"SenhaSegura123\",\"perfil\":\"OPERACIONAL\"}"))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/empresas/999999/usuarios").header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isForbidden());

        var target = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow();
        mvc.perform(post("/api/v1/empresas/" + companyId + "/usuarios/" + target.getId() + "/reset-senha")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"novaSenha\":\"ResetSenha123\"}"))
            .andExpect(status().isNoContent());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"senha\":\"ResetSenha123\"}"))
            .andExpect(status().isOk());
        target.setStatus(com.metalurgica.sistemagestaoindustrial.domain.StatusUsuario.INATIVO);
        usuarioRepository.save(target);
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"senha\":\"ResetSenha123\"}"))
            .andExpect(status().isUnauthorized());
    }

    private String loginToken(String userEmail) throws Exception {
        var body = mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + userEmail + "\",\"senha\":\"SenhaSegura123\"}"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return body.replaceFirst(".*\\\"accessToken\\\":\\\"([^\\\"]+)\\\".*", "$1");
    }
}
