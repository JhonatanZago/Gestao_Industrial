package com.metalurgica.sistemagestaoindustrial.resource;

import com.metalurgica.sistemagestaoindustrial.domain.StatusUsuario;
import com.metalurgica.sistemagestaoindustrial.dto.CreateUsuarioRequest;
import com.metalurgica.sistemagestaoindustrial.dto.UsuarioResponse;
import com.metalurgica.sistemagestaoindustrial.dto.ResetPasswordRequest;
import com.metalurgica.sistemagestaoindustrial.dto.AlterarPerfilRequest;
import com.metalurgica.sistemagestaoindustrial.repository.EmpresaRepository;
import com.metalurgica.sistemagestaoindustrial.repository.PerfilRepository;
import com.metalurgica.sistemagestaoindustrial.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/usuarios")
public class UsuarioResource {
    private final UsuarioService usuarioService;
    private final EmpresaRepository empresaRepository;
    private final PerfilRepository perfilRepository;

    public UsuarioResource(UsuarioService usuarioService, EmpresaRepository empresaRepository,
                           PerfilRepository perfilRepository) {
        this.usuarioService = usuarioService;
        this.empresaRepository = empresaRepository;
        this.perfilRepository = perfilRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USUARIO_VISUALIZAR')")
    public Page<UsuarioResponse> list(@PathVariable Long empresaId, @AuthenticationPrincipal Jwt jwt, Pageable pageable) {
        ensureCompany(empresaId, jwt);
        return usuarioService.listByCompany(empresaId, pageable).map(this::toResponse);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USUARIO_GERENCIAR')")
    public ResponseEntity<UsuarioResponse> create(@PathVariable Long empresaId, @AuthenticationPrincipal Jwt jwt,
                                                    @Valid @RequestBody CreateUsuarioRequest request) {
        ensureCompany(empresaId, jwt);
        var company = empresaRepository.findById(empresaId)
            .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada"));
        var profile = perfilRepository.findByCodigo(request.perfil())
            .orElseThrow(() -> new IllegalArgumentException("Perfil não encontrado"));
        var user = usuarioService.create(company, profile, request.nome(), request.email(), request.senha());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(user));
    }

    @PatchMapping("/{usuarioId}/status/{status}")
    @PreAuthorize("hasAuthority('USUARIO_GERENCIAR')")
    public UsuarioResponse updateStatus(@PathVariable Long empresaId, @PathVariable Long usuarioId,
                                        @PathVariable StatusUsuario status, @AuthenticationPrincipal Jwt jwt) {
        ensureCompany(empresaId, jwt);
        return toResponse(usuarioService.updateStatus(empresaId, usuarioId, status));
    }

    @PostMapping("/{usuarioId}/reset-senha")
    @PreAuthorize("hasAuthority('USUARIO_GERENCIAR')")
    public ResponseEntity<Void> resetPassword(@PathVariable Long empresaId, @PathVariable Long usuarioId,
                                              @Valid @RequestBody ResetPasswordRequest request,
                                              @AuthenticationPrincipal Jwt jwt) {
        ensureCompany(empresaId, jwt);
        usuarioService.resetPassword(empresaId, usuarioId, request.novaSenha());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{usuarioId}/perfil")
    @PreAuthorize("hasAuthority('USUARIO_GERENCIAR')")
    public UsuarioResponse updateProfile(@PathVariable Long empresaId, @PathVariable Long usuarioId,
                                         @Valid @RequestBody AlterarPerfilRequest request,
                                         @AuthenticationPrincipal Jwt jwt) {
        ensureCompany(empresaId, jwt);
        var profile = perfilRepository.findByCodigo(request.perfil())
                .orElseThrow(() -> new IllegalArgumentException("Perfil não encontrado"));
        return toResponse(usuarioService.updateProfile(empresaId, usuarioId, profile));
    }

    private void ensureCompany(Long empresaId, Jwt jwt) {
        var tokenCompany = ((Number) jwt.getClaim("empresaId")).longValue();
        if (!empresaId.equals(tokenCompany)) {
            throw new AccessDeniedException("Acesso negado");
        }
    }

    private UsuarioResponse toResponse(com.metalurgica.sistemagestaoindustrial.domain.Usuario user) {
        return new UsuarioResponse(user.getId(), user.getEmpresa().getId(), user.getNome(), user.getEmail(),
            user.getStatus().name(), user.getPerfil().getCodigo());
    }
}
