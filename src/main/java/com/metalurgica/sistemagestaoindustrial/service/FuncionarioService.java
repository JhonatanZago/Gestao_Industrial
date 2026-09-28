package com.metalurgica.sistemagestaoindustrial.service;

import com.metalurgica.sistemagestaoindustrial.domain.Funcionario;
import com.metalurgica.sistemagestaoindustrial.audit.*;
import com.metalurgica.sistemagestaoindustrial.dto.request.FuncionarioRequest;
import com.metalurgica.sistemagestaoindustrial.dto.response.FuncionarioResponse;
import com.metalurgica.sistemagestaoindustrial.exception.DuplicateResourceException;
import com.metalurgica.sistemagestaoindustrial.exception.ResourceNotFoundException;
import com.metalurgica.sistemagestaoindustrial.mapper.FuncionarioMapper;
import com.metalurgica.sistemagestaoindustrial.repository.FuncionarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.metalurgica.sistemagestaoindustrial.security.SectorScopeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class FuncionarioService {
    private final FuncionarioRepository repository;
    private final EmpresaService empresaService;
    private final FuncionarioMapper mapper;
    private final SectorScopeService sectorScopeService;
    private final AuditoriaService auditoriaService;

    public FuncionarioService(FuncionarioRepository repository, EmpresaService empresaService, FuncionarioMapper mapper, SectorScopeService sectorScopeService, AuditoriaService auditoriaService) {
        this.repository = repository; this.empresaService = empresaService; this.mapper = mapper; this.sectorScopeService = sectorScopeService; this.auditoriaService = auditoriaService;
    }

    @Transactional
    public FuncionarioResponse create(Long empresaId, FuncionarioRequest request) {
        var empresa = empresaService.find(empresaId); var codigo = normalize(request.codigo());
        ensureUnique(empresaId, codigo, null);
        var f = new Funcionario(); f.setEmpresa(empresa); f.setCodigo(codigo); f.setNome(EmpresaService.normalizeText(request.nome())); f.touchForCreate();
        var saved = repository.save(f); auditoriaService.record(empresaId, TipoRecursoAuditoria.FUNCIONARIO, saved.getId(), AcaoAuditoria.CRIACAO, OrigemAuditoria.MANUAL, null, snapshot(saved), null, null); return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<FuncionarioResponse> list(Long empresaId, Boolean ativo, String codigo, String nome, Pageable pageable) {
        empresaService.find(empresaId);
        Specification<Funcionario> spec = (root, query, cb) -> cb.equal(root.get("empresa").get("id"), empresaId);
        var scope = sectorScopeService.currentScope(empresaId);
        if (scope.isPresent()) {
            if (scope.get().isEmpty()) return Page.empty(pageable);
            spec = spec.and((r, q, c) -> {
                var subquery = q.subquery(Long.class);
                var production = subquery.from(com.metalurgica.sistemagestaoindustrial.domain.RegistroProducao.class);
                subquery.select(c.literal(1L)).where(c.equal(production.get("funcionario").get("id"), r.get("id")), production.get("setor").get("id").in(scope.get()));
                return c.exists(subquery);
            });
        }
        if (codigo != null && !codigo.isBlank()) spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("codigo")), "%" + codigo.trim().toLowerCase() + "%"));
        if (nome != null && !nome.isBlank()) spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("nome")), "%" + nome.trim().toLowerCase() + "%"));
        if (ativo != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("ativo"), ativo));
        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }


    @Transactional(readOnly = true)
    public Funcionario find(Long empresaId, Long funcionarioId) {
        empresaService.find(empresaId);
        return repository.findByIdAndEmpresaId(funcionarioId, empresaId).orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado nesta empresa."));
    }

    @Transactional(readOnly = true)
    public FuncionarioResponse get(Long empresaId, Long funcionarioId) { return mapper.toResponse(find(empresaId, funcionarioId)); }

    @Transactional
    public FuncionarioResponse update(Long empresaId, Long funcionarioId, FuncionarioRequest request) {
        var f = find(empresaId, funcionarioId); var codigo = normalize(request.codigo()); ensureUnique(empresaId, codigo, funcionarioId);
        var before = snapshot(f); f.setCodigo(codigo); f.setNome(EmpresaService.normalizeText(request.nome())); f.touchForUpdate(); var saved = repository.save(f); auditoriaService.record(empresaId, TipoRecursoAuditoria.FUNCIONARIO, funcionarioId, AcaoAuditoria.ALTERACAO, OrigemAuditoria.MANUAL, before, snapshot(saved), null, null); return mapper.toResponse(saved);
    }

    @Transactional
    public FuncionarioResponse setActive(Long empresaId, Long funcionarioId, boolean active) {
        var f = find(empresaId, funcionarioId); var before = snapshot(f); f.setAtivo(active); f.touchForUpdate(); var saved = repository.save(f); auditoriaService.record(empresaId, TipoRecursoAuditoria.FUNCIONARIO, funcionarioId, active ? AcaoAuditoria.ATIVACAO : AcaoAuditoria.DESATIVACAO, OrigemAuditoria.MANUAL, before, snapshot(saved), null, null); return mapper.toResponse(saved);
    }

    private void ensureUnique(Long empresaId, String codigo, Long id) {
        boolean exists = id == null ? repository.existsByEmpresaIdAndCodigo(empresaId, codigo) : repository.existsByEmpresaIdAndCodigoAndIdNot(empresaId, codigo, id);
        if (exists) throw new DuplicateResourceException("Já existe um funcionário com o código " + codigo + " nesta empresa.");
    }

    private static String normalize(String value) { return EmpresaService.normalizeText(value); }
    private static Map<String, Object> snapshot(Funcionario f) { var values = new LinkedHashMap<String, Object>(); values.put("id", f.getId()); values.put("codigo", f.getCodigo()); values.put("nome", f.getNome()); values.put("ativo", f.isAtivo()); return values; }
}
