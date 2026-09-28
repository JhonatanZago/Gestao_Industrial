package com.metalurgica.sistemagestaoindustrial.resource;

import com.metalurgica.sistemagestaoindustrial.dto.LoginRequest;
import com.metalurgica.sistemagestaoindustrial.dto.LoginResponse;
import com.metalurgica.sistemagestaoindustrial.dto.ChangePasswordRequest;
import com.metalurgica.sistemagestaoindustrial.dto.MeResponse;
import com.metalurgica.sistemagestaoindustrial.security.JwtTokenService;
import com.metalurgica.sistemagestaoindustrial.service.UsuarioService;
import com.metalurgica.sistemagestaoindustrial.repository.UsuarioSetorRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthResource {
    private final UsuarioService usuarioService;
    private final JwtTokenService tokenService;
    private final UsuarioSetorRepository usuarioSetorRepository;

    public AuthResource(UsuarioService usuarioService, JwtTokenService tokenService,
                        UsuarioSetorRepository usuarioSetorRepository) {
        this.usuarioService = usuarioService;
        this.tokenService = tokenService;
        this.usuarioSetorRepository = usuarioSetorRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var user = usuarioService.authenticate(request.email(), request.senha());
        return ResponseEntity.ok(new LoginResponse(tokenService.issue(user), "Bearer", tokenService.getExpirationSeconds()));
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal Jwt jwt) {
        var user = usuarioService.findActiveById(Long.valueOf(jwt.getSubject()));
        return new MeResponse(
            user.getId(), user.getNome(), user.getEmail(), user.getEmpresa().getId(),
            user.getPerfil().getCodigo(),
            user.getPerfil().getPermissoes().stream().map(p -> p.getCodigo()).sorted().toList(),
            usuarioSetorRepository.findByUsuarioId(user.getId()).stream().map(a -> a.getSetor().getId()).sorted().toList());
    }

    @PatchMapping("/senha")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal Jwt jwt,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        usuarioService.changePassword(Long.valueOf(jwt.getSubject()), request.senhaAtual(), request.novaSenha());
        return ResponseEntity.noContent().build();
    }
}
