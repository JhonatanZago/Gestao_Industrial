package com.metalurgica.sistemagestaoindustrial.assistente.security;

import com.metalurgica.sistemagestaoindustrial.security.SectorScopeService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AssistenteSecurityContextResolver {
    private final SectorScopeService sectorScopeService;

    public AssistenteSecurityContextResolver(SectorScopeService sectorScopeService) {
        this.sectorScopeService = sectorScopeService;
    }

    public AssistenteSecurityContext resolve(Authentication authentication, Long empresaId) {
        if (!(authentication instanceof JwtAuthenticationToken jwt)) {
            throw new AccessDeniedException("Autenticação necessária");
        }
        var claimEmpresa = jwt.getToken().getClaim("empresaId");
        var subject = jwt.getToken().getSubject();
        if (!(claimEmpresa instanceof Number number) || !empresaId.equals(number.longValue())
                || subject == null || subject.isBlank()) {
            throw new AccessDeniedException("Empresa fora do escopo autorizado");
        }
        final Long userId;
        try {
            userId = Long.valueOf(subject);
        } catch (NumberFormatException exception) {
            throw new AccessDeniedException("Identidade autenticada inválida");
        }
        var scope = sectorScopeService.currentScope(empresaId);
        Set<Long> sectors = scope.orElseGet(Set::of);
        var allSectors = scope.isEmpty();
        var permissions = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toUnmodifiableSet());
        return new AssistenteSecurityContext(userId, empresaId,
                jwt.getToken().getClaimAsString("perfil"), permissions, sectors, allSectors);
    }
}
