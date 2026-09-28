package com.metalurgica.sistemagestaoindustrial.service;

import com.metalurgica.sistemagestaoindustrial.domain.Peca;
import com.metalurgica.sistemagestaoindustrial.audit.*;
import com.metalurgica.sistemagestaoindustrial.dto.request.PecaRequest;
import com.metalurgica.sistemagestaoindustrial.dto.response.PecaResponse;
import com.metalurgica.sistemagestaoindustrial.exception.DuplicateResourceException;
import com.metalurgica.sistemagestaoindustrial.exception.ResourceNotFoundException;
import com.metalurgica.sistemagestaoindustrial.mapper.PecaMapper;
import com.metalurgica.sistemagestaoindustrial.repository.PecaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PecaService {
    private final PecaRepository repository;
    private final EmpresaService empresaService;
    private final PecaMapper mapper;
    private final AuditoriaService auditoriaService;

    public PecaService(PecaRepository repository, EmpresaService empresaService, PecaMapper mapper, AuditoriaService auditoriaService) {
        this.repository = repository; this.empresaService = empresaService; this.mapper = mapper; this.auditoriaService = auditoriaService;
    }

    @Transactional
    public PecaResponse create(Long empresaId, PecaRequest request) {
        var empresa = empresaService.find(empresaId); var codigo = normalize(request.codigo()); ensureUnique(empresaId, codigo, null);
        var p = new Peca(); p.setEmpresa(empresa); p.setCodigo(codigo); p.setDescricao(EmpresaService.normalizeText(request.descricao())); p.touchForCreate();
        var saved = repository.save(p); auditoriaService.record(empresaId, TipoRecursoAuditoria.PECA, saved.getId(), AcaoAuditoria.CRIACAO, OrigemAuditoria.MANUAL, null, snapshot(saved), null, null); return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<PecaResponse> list(Long empresaId, Boolean ativo, String codigo, String descricao, Pageable pageable) {
        empresaService.find(empresaId);
        Page<Peca> page;
        if (codigo != null && !codigo.isBlank()) page = repository.findByEmpresaIdAndCodigoContainingIgnoreCase(empresaId, codigo.trim(), pageable);
        else if (descricao != null && !descricao.isBlank()) page = repository.findByEmpresaIdAndDescricaoContainingIgnoreCase(empresaId, descricao.trim(), pageable);
        else if (ativo != null) page = repository.findByEmpresaIdAndAtivo(empresaId, ativo, pageable);
        else page = repository.findByEmpresaId(empresaId, pageable);
        return page.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Peca find(Long empresaId, Long pecaId) {
        empresaService.find(empresaId);
        return repository.findByIdAndEmpresaId(pecaId, empresaId).orElseThrow(() -> new ResourceNotFoundException("Peça não encontrada nesta empresa."));
    }

    @Transactional(readOnly = true)
    public PecaResponse get(Long empresaId, Long pecaId) { return mapper.toResponse(find(empresaId, pecaId)); }

    @Transactional
    public PecaResponse update(Long empresaId, Long pecaId, PecaRequest request) {
        var p = find(empresaId, pecaId); var codigo = normalize(request.codigo()); ensureUnique(empresaId, codigo, pecaId);
        var before = snapshot(p); p.setCodigo(codigo); p.setDescricao(EmpresaService.normalizeText(request.descricao())); p.touchForUpdate(); var saved = repository.save(p); auditoriaService.record(empresaId, TipoRecursoAuditoria.PECA, pecaId, AcaoAuditoria.ALTERACAO, OrigemAuditoria.MANUAL, before, snapshot(saved), null, null); return mapper.toResponse(saved);
    }

    @Transactional
    public PecaResponse setActive(Long empresaId, Long pecaId, boolean active) {
        var p = find(empresaId, pecaId); var before = snapshot(p); p.setAtivo(active); p.touchForUpdate(); var saved = repository.save(p); auditoriaService.record(empresaId, TipoRecursoAuditoria.PECA, pecaId, active ? AcaoAuditoria.ATIVACAO : AcaoAuditoria.DESATIVACAO, OrigemAuditoria.MANUAL, before, snapshot(saved), null, null); return mapper.toResponse(saved);
    }

    private void ensureUnique(Long empresaId, String codigo, Long id) {
        boolean exists = id == null ? repository.existsByEmpresaIdAndCodigo(empresaId, codigo) : repository.existsByEmpresaIdAndCodigoAndIdNot(empresaId, codigo, id);
        if (exists) throw new DuplicateResourceException("Já existe uma peça com o código " + codigo + " nesta empresa.");
    }

    private static String normalize(String value) { return EmpresaService.normalizeText(value); }
    private static Map<String, Object> snapshot(Peca p) { var values = new LinkedHashMap<String, Object>(); values.put("id", p.getId()); values.put("codigo", p.getCodigo()); values.put("descricao", p.getDescricao()); values.put("ativo", p.isAtivo()); return values; }
}
