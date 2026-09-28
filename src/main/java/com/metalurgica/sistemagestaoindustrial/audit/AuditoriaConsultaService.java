package com.metalurgica.sistemagestaoindustrial.audit;

import com.metalurgica.sistemagestaoindustrial.dto.request.AuditoriaFiltro;
import com.metalurgica.sistemagestaoindustrial.dto.response.*;
import com.metalurgica.sistemagestaoindustrial.exception.ResourceNotFoundException;
import com.metalurgica.sistemagestaoindustrial.security.AuthorizationService;
import com.metalurgica.sistemagestaoindustrial.security.SectorScopeService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AuditoriaConsultaService {
    private static final Pattern FIELD = Pattern.compile("\\\"([^\\\"]+)\\\":(\\\"(?:\\\\.|[^\\\"])*\\\"|[^,}]+)");
    private final EventoAuditoriaRepository repository;
    private final AuthorizationService authorizationService;
    private final SectorScopeService sectorScopeService;
    private final int maxPageSize;

    public AuditoriaConsultaService(EventoAuditoriaRepository repository, AuthorizationService authorizationService,
                                    SectorScopeService sectorScopeService,
                                    @Value("${audit.max-page-size:100}") int maxPageSize) {
        this.repository = repository; this.authorizationService = authorizationService;
        this.sectorScopeService = sectorScopeService; this.maxPageSize = maxPageSize;
    }

    @Transactional(readOnly = true)
    public Page<EventoAuditoriaResumoResponse> list(Long empresaId, AuditoriaFiltro filter,
                                                    Pageable pageable, Authentication authentication) {
        authorize(empresaId, filter.tipoRecurso(), authentication);
        var page = repository.findAll(specification(empresaId, filter, authentication), bounded(pageable));
        return page.map(this::summary);
    }

    @Transactional(readOnly = true)
    public Page<EventoAuditoriaDetalheResponse> resourceHistory(Long empresaId, TipoRecursoAuditoria resource,
                                                                Long resourceId, Pageable pageable,
                                                                Authentication authentication) {
        authorize(empresaId, resource, authentication);
        var filter = new AuditoriaFiltro(null, null, null, resource, resourceId, null, null, null);
        return repository.findAll(specification(empresaId, filter, authentication), bounded(pageable)).map(event -> detail(event, authentication));
    }

    @Transactional(readOnly = true)
    public EventoAuditoriaDetalheResponse event(Long empresaId, Long eventId, Authentication authentication) {
        var event = repository.findById(eventId).filter(candidate -> Objects.equals(candidate.getEmpresaId(), empresaId))
                .orElseThrow(() -> new ResourceNotFoundException("Evento de auditoria não encontrado nesta empresa."));
        authorize(empresaId, event.getTipoRecurso(), authentication);
        if (!allowedBySector(event, authentication)) throw new org.springframework.security.access.AccessDeniedException("Evento fora do escopo autorizado");
        return detail(event, authentication);
    }

    private Specification<EventoAuditoria> specification(Long empresaId, AuditoriaFiltro f, Authentication authentication) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(cb.equal(root.get("empresaId"), empresaId));
            if (f.inicio() != null) predicates.add(cb.greaterThanOrEqualTo(root.get("dataHora"), f.inicio()));
            if (f.fim() != null) predicates.add(cb.lessThanOrEqualTo(root.get("dataHora"), f.fim()));
            if (f.usuarioId() != null) predicates.add(cb.equal(root.get("usuarioId"), f.usuarioId()));
            if (f.tipoRecurso() != null) predicates.add(cb.equal(root.get("tipoRecurso"), f.tipoRecurso()));
            if (f.recursoId() != null) predicates.add(cb.equal(root.get("recursoId"), f.recursoId()));
            if (f.acao() != null) predicates.add(cb.equal(root.get("acao"), f.acao()));
            if (f.origem() != null) predicates.add(cb.equal(root.get("origem"), f.origem()));
            if (f.correlationId() != null && !f.correlationId().isBlank()) predicates.add(cb.equal(root.get("correlationId"), f.correlationId()));
            var scope = sectorScopeService.currentScope(empresaId);
            if (scope.isPresent()) {
                if (f.tipoRecurso() != null && !sectorResource(f.tipoRecurso())) return cb.disjunction();
                var sectorPredicates = scope.get().stream().map(id -> cb.like(root.get("metadados"), "%\"setorId\":" + id + "%")).toList();
                if (sectorPredicates.isEmpty()) return cb.disjunction();
                predicates.add(cb.or(sectorPredicates.toArray(Predicate[]::new)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void authorize(Long empresaId, TipoRecursoAuditoria resource, Authentication authentication) {
        if (!authorizationService.belongsToCompany(authentication, empresaId)) throw new org.springframework.security.access.AccessDeniedException("Empresa fora do escopo autorizado");
        if (resource == TipoRecursoAuditoria.USUARIO || resource == TipoRecursoAuditoria.PERFIL || resource == TipoRecursoAuditoria.PERMISSAO) {
            require(authentication, "AUDITORIA_USUARIOS_VISUALIZAR");
        }
        if (resource == TipoRecursoAuditoria.VALOR_PRODUCAO) require(authentication, "AUDITORIA_FINANCEIRA_VISUALIZAR");
    }

    private void require(Authentication authentication, String permission) {
        if (!authorizationService.hasPermission(authentication, permission)) throw new org.springframework.security.access.AccessDeniedException("Permissão de auditoria insuficiente");
    }

    private boolean allowedBySector(EventoAuditoria event, Authentication authentication) {
        var scope = sectorScopeService.currentScope(event.getEmpresaId());
        if (scope.isEmpty()) return true;
        return scope.get().stream().anyMatch(id -> event.getMetadados() != null && event.getMetadados().contains("\"setorId\":" + id));
    }

    private boolean sectorResource(TipoRecursoAuditoria resource) {
        return resource == TipoRecursoAuditoria.SETOR || resource == TipoRecursoAuditoria.FUNCIONARIO
                || resource == TipoRecursoAuditoria.PECA || resource == TipoRecursoAuditoria.MAQUINA
                || resource == TipoRecursoAuditoria.TURNO || resource == TipoRecursoAuditoria.REGISTRO_PRODUCAO
                || resource == TipoRecursoAuditoria.PARADA || resource == TipoRecursoAuditoria.META_PRODUCAO
                || resource == TipoRecursoAuditoria.TEMPO_PADRAO || resource == TipoRecursoAuditoria.IMPORTACAO;
    }

    private Pageable bounded(Pageable pageable) {
        if (pageable.getPageSize() < 1 || pageable.getPageSize() > maxPageSize) throw new IllegalArgumentException("Tamanho de página inválido.");
        var sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "dataHora");
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private EventoAuditoriaResumoResponse summary(EventoAuditoria e) {
        return new EventoAuditoriaResumoResponse(e.getId(), e.getEmpresaId(), e.getUsuarioId(), e.getUsuarioNomeSnapshot(), e.getTipoRecurso(), e.getRecursoId(), e.getAcao(), e.getOrigem(), e.getDataHora(), e.getCorrelationId());
    }

    private EventoAuditoriaDetalheResponse detail(EventoAuditoria e, Authentication authentication) {
        var financial = e.getTipoRecurso() == TipoRecursoAuditoria.VALOR_PRODUCAO && !authorizationService.hasPermission(authentication, "AUDITORIA_FINANCEIRA_VISUALIZAR");
        var before = financial ? null : e.getDadosAnteriores(); var after = financial ? null : e.getDadosNovos();
        return new EventoAuditoriaDetalheResponse(e.getId(), e.getEmpresaId(), e.getUsuarioId(), e.getUsuarioNomeSnapshot(), e.getTipoRecurso(), e.getRecursoId(), e.getAcao(), e.getOrigem(), e.getDataHora(), before, after, e.getMetadados(), e.getCorrelationId(), diff(before, after));
    }

    private List<AlteracaoCampoResponse> diff(String before, String after) {
        var oldValues = parse(before); var newValues = parse(after); var keys = new TreeSet<String>(); keys.addAll(oldValues.keySet()); keys.addAll(newValues.keySet());
        return keys.stream().filter(key -> !Objects.equals(oldValues.get(key), newValues.get(key))).map(key -> new AlteracaoCampoResponse(key, oldValues.get(key), newValues.get(key))).toList();
    }

    private Map<String, String> parse(String json) {
        if (json == null || json.isBlank()) return Map.of();
        var values = new LinkedHashMap<String, String>(); Matcher matcher = FIELD.matcher(json);
        while (matcher.find()) values.put(matcher.group(1), matcher.group(2));
        return values;
    }
}
