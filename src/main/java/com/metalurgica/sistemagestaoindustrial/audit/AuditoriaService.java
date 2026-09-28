package com.metalurgica.sistemagestaoindustrial.audit;

import com.metalurgica.sistemagestaoindustrial.repository.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;

@Service
public class AuditoriaService {
    private final EventoAuditoriaRepository repository;
    private final AuditoriaSnapshotSanitizer sanitizer;
    private final UsuarioRepository usuarioRepository;

    public AuditoriaService(EventoAuditoriaRepository repository, AuditoriaSnapshotSanitizer sanitizer,
                            UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.sanitizer = sanitizer;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    /**
     * Ponto central chamado pelos serviÃ§os transacionais depois da validaÃ§Ã£o e persistÃªncia
     * da operaÃ§Ã£o. Controllers/resources apenas iniciam o fluxo; nenhum deles Ã© a Ãºnica
     * integraÃ§Ã£o de auditoria. A transaÃ§Ã£o do evento acompanha a transaÃ§Ã£o de negÃ³cio.
     */
    public EventoAuditoria record(Long empresaId, TipoRecursoAuditoria resource, Long resourceId,
                                  AcaoAuditoria action, OrigemAuditoria origin,
                                  Map<String, ?> before, Map<String, ?> after,
                                  Map<String, ?> metadata, String correlationId) {
        var actor = actor(empresaId);
        var effectiveOrigin = actor.usuarioId() == null && origin == OrigemAuditoria.MANUAL
                ? OrigemAuditoria.SISTEMA : origin;
        var event = new EventoAuditoria(
                empresaId, actor.usuarioId(), actor.nome(), resource, resourceId, action,
                effectiveOrigin, OffsetDateTime.now(), sanitizer.sanitize(resource, before),
                sanitizer.sanitize(resource, after), sanitizer.sanitize(TipoRecursoAuditoria.IMPORTACAO, metadata),
                correlationId);
        return repository.save(event);
    }

    @Transactional
    public EventoAuditoria recordPasswordChange(Long empresaId, Long targetUserId, OrigemAuditoria origin) {
        return record(empresaId, TipoRecursoAuditoria.USUARIO, targetUserId,
                AcaoAuditoria.ALTERACAO_SENHA, origin, null, null, null, null);
    }

    private Actor actor(Long empresaId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwt)) return new Actor(null, null);
        Long userId = null;
        Long companyId = null;
        try { userId = Long.valueOf(jwt.getToken().getSubject()); } catch (RuntimeException ignored) { }
        var companyClaim = jwt.getToken().getClaim("empresaId");
        if (companyClaim instanceof Number number) companyId = number.longValue();
        if (companyId != null && !companyId.equals(empresaId)) {
            throw new IllegalArgumentException("Ator não pertence à empresa auditada.");
        }
        String name = null;
        if (userId != null) name = usuarioRepository.findById(userId).map(u -> u.getNome()).orElse(null);
        return new Actor(userId, name);
    }

    private record Actor(Long usuarioId, String nome) { }
}
