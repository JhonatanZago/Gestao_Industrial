package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.domain.Empresa;
import com.metalurgica.sistemagestaoindustrial.repository.EmpresaRepository;
import com.metalurgica.sistemagestaoindustrial.repository.PerfilRepository;
import com.metalurgica.sistemagestaoindustrial.repository.UsuarioRepository;
import com.metalurgica.sistemagestaoindustrial.security.JwtTokenService;
import com.metalurgica.sistemagestaoindustrial.service.UsuarioService;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Test-only adapter that authenticates legacy requests with a real signed JWT. */
final class LegacyAuthenticatedMockMvc {
    private static final Pattern COMPANY = Pattern.compile("/api/v1/empresas/(\\d+)(?:/|$)");
    private final MockMvc mvc;
    private final EmpresaRepository empresaRepository;
    private final PerfilRepository perfilRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final JwtTokenService tokenService;
    private final Map<Long, String> tokens = new ConcurrentHashMap<>();
    private volatile String rootToken;

    LegacyAuthenticatedMockMvc(MockMvc mvc, EmpresaRepository empresaRepository,
                               PerfilRepository perfilRepository, UsuarioRepository usuarioRepository,
                               UsuarioService usuarioService, JwtTokenService tokenService) {
        this.mvc = mvc;
        this.empresaRepository = empresaRepository;
        this.perfilRepository = perfilRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.tokenService = tokenService;
    }

    ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return mvc.perform(builder.with(authenticateByCompany()));
    }

    ResultActions perform(MockMultipartHttpServletRequestBuilder builder) throws Exception {
        return mvc.perform(builder.with(authenticateByCompany()));
    }

    private RequestPostProcessor authenticateByCompany() {
        return request -> {
            Matcher matcher = COMPANY.matcher(request.getRequestURI());
            if (matcher.find()) {
                var companyId = Long.valueOf(matcher.group(1));
                var token = empresaRepository.existsById(companyId) ? tokenFor(companyId) : rootToken();
                request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            } else if (request.getRequestURI().equals("/api/v1/empresas")
                    || request.getRequestURI().startsWith("/actuator/")
                    || request.getRequestURI().startsWith("/api/v1/operacao/")) {
                request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + rootToken());
            }
            return request;
        };
    }

    private String tokenFor(long companyId) {
        return tokens.computeIfAbsent(companyId, id -> {
            Empresa empresa = empresaRepository.findById(id).orElseThrow();
            String email = "legacy-admin-" + id + "@example.test";
            var usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> usuarioService.create(empresa,
                    perfilRepository.findByCodigo("ADMINISTRADOR").orElseThrow(),
                    "Administrador de teste", email, "SenhaSegura123"));
            return tokenService.issue(usuario);
        });
    }

    private String rootToken() {
        if (rootToken != null) return rootToken;
        synchronized (this) {
            if (rootToken == null) {
                Empresa empresa = empresaRepository.findAll().stream().findFirst().orElseGet(() -> {
                    var seed = new Empresa();
                    seed.setRazaoSocial("Empresa de teste para onboarding");
                    seed.setNomeFantasia("Onboarding");
                    seed.setCnpj(String.format("99%012d", Math.abs(System.nanoTime() % 1000000000000L)));
                    seed.touchForCreate();
                    return empresaRepository.save(seed);
                });
                rootToken = tokenFor(empresa.getId());
            }
            return rootToken;
        }
    }
}
