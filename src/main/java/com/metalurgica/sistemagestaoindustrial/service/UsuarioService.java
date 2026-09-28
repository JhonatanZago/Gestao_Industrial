package com.metalurgica.sistemagestaoindustrial.service;

import com.metalurgica.sistemagestaoindustrial.domain.Empresa;
import com.metalurgica.sistemagestaoindustrial.audit.*;
import com.metalurgica.sistemagestaoindustrial.domain.Perfil;
import com.metalurgica.sistemagestaoindustrial.domain.StatusUsuario;
import com.metalurgica.sistemagestaoindustrial.domain.Usuario;
import com.metalurgica.sistemagestaoindustrial.repository.UsuarioRepository;
import com.metalurgica.sistemagestaoindustrial.security.PasswordProperties;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class UsuarioService {
    private static final String INVALID_CREDENTIALS = "Credenciais inválidas";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordProperties passwordProperties;
    private final AuditoriaService auditoriaService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          PasswordProperties passwordProperties, AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordProperties = passwordProperties;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public Usuario authenticate(String email, String rawPassword) {
        var normalizedEmail = normalizeEmail(email);
        var user = usuarioRepository.findByEmailIgnoreCase(normalizedEmail)
            .orElseThrow(() -> new BadCredentialsException(INVALID_CREDENTIALS));
        if (user.getStatus() != StatusUsuario.ATIVO
            || !passwordEncoder.matches(rawPassword, user.getSenhaHash())) {
            throw new BadCredentialsException(INVALID_CREDENTIALS);
        }
        return user;
    }

    @Transactional
    public Usuario create(Empresa empresa, Perfil perfil, String nome, String email, String rawPassword) {
        validatePassword(rawPassword);
        var normalizedEmail = normalizeEmail(email);
        if (usuarioRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }
        var user = new Usuario();
        user.setEmpresa(empresa);
        user.setPerfil(perfil);
        user.setNome(nome.trim());
        user.setEmail(normalizedEmail);
        user.setSenhaHash(passwordEncoder.encode(rawPassword));
        user.setStatus(StatusUsuario.ATIVO);
        var saved = usuarioRepository.save(user);
        auditoriaService.record(empresa.getId(), TipoRecursoAuditoria.USUARIO, saved.getId(), AcaoAuditoria.CRIACAO, OrigemAuditoria.MANUAL, null, userSnapshot(saved), null, null);
        return saved;
    }

    public void validatePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < passwordProperties.getMinLength()) {
            throw new IllegalArgumentException("Senha não atende à política mínima");
        }
    }

    @Transactional(readOnly = true)
    public Usuario findActiveById(Long id) {
        var user = usuarioRepository.findById(id)
            .orElseThrow(() -> new BadCredentialsException(INVALID_CREDENTIALS));
        if (user.getStatus() != StatusUsuario.ATIVO) {
            throw new BadCredentialsException(INVALID_CREDENTIALS);
        }
        return user;
    }

    @Transactional
    public void changePassword(Long id, String currentPassword, String newPassword) {
        var user = findActiveById(id);
        if (!passwordEncoder.matches(currentPassword, user.getSenhaHash())) {
            throw new BadCredentialsException(INVALID_CREDENTIALS);
        }
        validatePassword(newPassword);
        user.setSenhaHash(passwordEncoder.encode(newPassword));
        user.setCredenciaisVersao(user.getCredenciaisVersao() + 1);
        usuarioRepository.save(user);
        auditoriaService.record(user.getEmpresa().getId(), TipoRecursoAuditoria.USUARIO, id, AcaoAuditoria.ALTERACAO_SENHA, OrigemAuditoria.MANUAL, null, null, null, null);
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private static java.util.Map<String, Object> userSnapshot(Usuario user) {
        var values = new java.util.LinkedHashMap<String, Object>();
        values.put("id", user.getId()); values.put("empresaId", user.getEmpresa().getId());
        values.put("perfilId", user.getPerfil().getId()); values.put("nome", user.getNome());
        values.put("email", user.getEmail()); values.put("status", user.getStatus());
        values.put("credenciaisVersao", user.getCredenciaisVersao());
        return values;
    }

    @Transactional(readOnly = true)
    public Page<Usuario> listByCompany(Long empresaId, Pageable pageable) {
        return usuarioRepository.findByEmpresaId(empresaId, pageable);
    }

    @Transactional
    public Usuario updateStatus(Long empresaId, Long userId, StatusUsuario status) {
        var user = usuarioRepository.findById(userId)
            .filter(candidate -> candidate.getEmpresa().getId().equals(empresaId))
            .orElseThrow(() -> new IllegalArgumentException("Usuário não pertence à empresa informada"));
        var before = userSnapshot(user); user.setStatus(status);
        if (status != StatusUsuario.ATIVO) {
            user.setCredenciaisVersao(user.getCredenciaisVersao() + 1);
        }
        var saved = usuarioRepository.save(user);
        auditoriaService.record(empresaId, TipoRecursoAuditoria.USUARIO, userId,
                status == StatusUsuario.ATIVO ? AcaoAuditoria.ATIVACAO : AcaoAuditoria.DESATIVACAO,
                OrigemAuditoria.MANUAL, before, userSnapshot(saved), null, null);
        return saved;
    }

    @Transactional
    public Usuario updateProfile(Long empresaId, Long userId, Perfil profile) {
        var user = usuarioRepository.findById(userId)
                .filter(candidate -> candidate.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new IllegalArgumentException("Usuário não pertence à empresa informada"));
        var before = userSnapshot(user);
        user.setPerfil(profile);
        var saved = usuarioRepository.save(user);
        auditoriaService.record(empresaId, TipoRecursoAuditoria.PERFIL, userId, AcaoAuditoria.ALTERACAO_PERMISSAO,
                OrigemAuditoria.MANUAL, before, userSnapshot(saved), null, null);
        return saved;
    }

    @Transactional
    public void resetPassword(Long empresaId, Long userId, String newPassword) {
        var user = usuarioRepository.findById(userId)
            .filter(candidate -> candidate.getEmpresa().getId().equals(empresaId))
            .orElseThrow(() -> new IllegalArgumentException("Usuário não pertence à empresa informada"));
        validatePassword(newPassword);
        user.setSenhaHash(passwordEncoder.encode(newPassword));
        user.setCredenciaisVersao(user.getCredenciaisVersao() + 1);
        usuarioRepository.save(user);
        auditoriaService.record(empresaId, TipoRecursoAuditoria.USUARIO, userId, AcaoAuditoria.ALTERACAO_SENHA, OrigemAuditoria.MANUAL, null, null, null, null);
    }
}
