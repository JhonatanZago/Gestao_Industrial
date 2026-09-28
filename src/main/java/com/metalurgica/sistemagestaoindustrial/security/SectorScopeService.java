package com.metalurgica.sistemagestaoindustrial.security;

import com.metalurgica.sistemagestaoindustrial.repository.SetorRepository;
import com.metalurgica.sistemagestaoindustrial.repository.UsuarioSetorRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.Optional;
import java.util.Set;

@Service
public class SectorScopeService {
    private final SetorRepository setorRepository;
    private final UsuarioSetorRepository usuarioSetorRepository;

    public SectorScopeService(SetorRepository setorRepository, UsuarioSetorRepository usuarioSetorRepository) {
        this.setorRepository = setorRepository;
        this.usuarioSetorRepository = usuarioSetorRepository;
    }

    public boolean canAccess(Long userId, String profile, Long companyId, Long sectorId) {
        if (setorRepository.findByIdAndEmpresaId(sectorId, companyId).isEmpty()) return false;
        if ("ADMINISTRADOR".equals(profile)) return true;
        return usuarioSetorRepository.existsByUsuarioIdAndSetorId(userId, sectorId);
    }

    public Optional<Set<Long>> currentScope(Long companyId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwt)) return Optional.empty();
        var tokenCompany = jwt.getToken().getClaim("empresaId");
        if (!(tokenCompany instanceof Number number) || !companyId.equals(number.longValue())) return Optional.of(Set.of());
        if ("ADMINISTRADOR".equals(jwt.getToken().getClaimAsString("perfil"))) return Optional.empty();
        var userId = Long.valueOf(jwt.getToken().getSubject());
        return Optional.of(usuarioSetorRepository.findByUsuarioId(userId).stream()
            .map(association -> association.getSetor().getId()).collect(java.util.stream.Collectors.toUnmodifiableSet()));
    }

    public Predicate restrict(Root<?> root, CriteriaBuilder cb, Long companyId, String sectorAttribute) {
        var scope = currentScope(companyId);
        if (scope.isEmpty()) return cb.conjunction();
        if (scope.get().isEmpty()) return cb.disjunction();
        return root.get(sectorAttribute).get("id").in(scope.get());
    }

    public Predicate restrictIncludingGlobal(Root<?> root, CriteriaBuilder cb, Long companyId, String sectorAttribute) {
        var scope = currentScope(companyId);
        if (scope.isEmpty()) return cb.conjunction();
        if (scope.get().isEmpty()) return cb.isNull(root.get(sectorAttribute));
        return cb.or(cb.isNull(root.get(sectorAttribute)), root.get(sectorAttribute).get("id").in(scope.get()));
    }

    public void assertAllowed(Long companyId, Long sectorId) {
        if (sectorId == null) return;
        var scope = currentScope(companyId);
        if (scope.isPresent() && !scope.get().contains(sectorId)) {
            throw new org.springframework.security.access.AccessDeniedException("Setor fora do escopo autorizado");
        }
    }
}
