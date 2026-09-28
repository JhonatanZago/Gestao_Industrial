package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.repository.EmpresaRepository;
import com.metalurgica.sistemagestaoindustrial.repository.PerfilRepository;
import com.metalurgica.sistemagestaoindustrial.repository.UsuarioRepository;
import com.metalurgica.sistemagestaoindustrial.security.JwtTokenService;
import com.metalurgica.sistemagestaoindustrial.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.BeforeEach;

abstract class AuthenticatedIntegrationTest {
    @Autowired MockMvc rawMvc;
    @Autowired EmpresaRepository empresaRepository;
    @Autowired PerfilRepository perfilRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired UsuarioService usuarioService;
    @Autowired JwtTokenService tokenService;
    protected LegacyAuthenticatedMockMvc mvc;

    @BeforeEach
    void initializeAuthenticatedMvc() {
        mvc = new LegacyAuthenticatedMockMvc(rawMvc, empresaRepository, perfilRepository,
            usuarioRepository, usuarioService, tokenService);
    }
}
