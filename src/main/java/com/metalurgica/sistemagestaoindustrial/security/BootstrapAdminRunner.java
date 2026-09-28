package com.metalurgica.sistemagestaoindustrial.security;

import com.metalurgica.sistemagestaoindustrial.repository.EmpresaRepository;
import com.metalurgica.sistemagestaoindustrial.repository.PerfilRepository;
import com.metalurgica.sistemagestaoindustrial.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BootstrapAdminRunner implements CommandLineRunner {
    private final boolean enabled;
    private final String email;
    private final String password;
    private final String name;
    private final String empresaId;
    private final EmpresaRepository empresaRepository;
    private final PerfilRepository perfilRepository;
    private final com.metalurgica.sistemagestaoindustrial.repository.UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public BootstrapAdminRunner(
        @Value("${security.bootstrap-admin.enabled:false}") boolean enabled,
        @Value("${security.bootstrap-admin.email:}") String email,
        @Value("${security.bootstrap-admin.password:}") String password,
        @Value("${security.bootstrap-admin.nome:Administrador inicial}") String name,
        @Value("${security.bootstrap-admin.empresa-id:}") String empresaId,
        EmpresaRepository empresaRepository,
        PerfilRepository perfilRepository,
        com.metalurgica.sistemagestaoindustrial.repository.UsuarioRepository usuarioRepository,
        UsuarioService usuarioService) {
        this.enabled = enabled;
        this.email = email;
        this.password = password;
        this.name = name;
        this.empresaId = empresaId;
        this.empresaRepository = empresaRepository;
        this.perfilRepository = perfilRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        if (email.isBlank() || password.isBlank() || empresaId.isBlank()) {
            throw new IllegalStateException("Bootstrap administrativo exige e-mail, senha e BOOTSTRAP_ADMIN_EMPRESA_ID");
        }
        var company = empresaRepository.findById(Long.valueOf(empresaId))
            .orElseThrow(() -> new IllegalStateException("Empresa do bootstrap administrativo não encontrada"));
        if (usuarioRepository.existsByEmailIgnoreCase(UsuarioService.normalizeEmail(email))) {
            return;
        }
        var profile = perfilRepository.findByCodigo("ADMINISTRADOR")
            .orElseThrow(() -> new IllegalStateException("Perfil ADMINISTRADOR não foi semeado"));
        usuarioService.create(company, profile, name, email, password);
    }
}
