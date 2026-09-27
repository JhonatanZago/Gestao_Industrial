package com.metalurgica.sistemagestaoindustrial.service;

import com.metalurgica.sistemagestaoindustrial.domain.MotivoParada;
import com.metalurgica.sistemagestaoindustrial.dto.request.MotivoParadaRequest;
import com.metalurgica.sistemagestaoindustrial.dto.response.MotivoParadaResponse;
import com.metalurgica.sistemagestaoindustrial.exception.DuplicateResourceException;
import com.metalurgica.sistemagestaoindustrial.exception.ResourceNotFoundException;
import com.metalurgica.sistemagestaoindustrial.mapper.MotivoParadaMapper;
import com.metalurgica.sistemagestaoindustrial.repository.MotivoParadaRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service public class MotivoParadaService {
    private final MotivoParadaRepository repository; private final EmpresaService empresaService; private final MotivoParadaMapper mapper;
    public MotivoParadaService(MotivoParadaRepository repository, EmpresaService empresaService, MotivoParadaMapper mapper) { this.repository = repository; this.empresaService = empresaService; this.mapper = mapper; }
    @Transactional public MotivoParadaResponse create(Long empresaId, MotivoParadaRequest req) {
        var empresa = empresaService.find(empresaId); var codigo = text(req.codigo()).toUpperCase(java.util.Locale.ROOT); ensureUnique(empresaId, codigo, null);
        var m = new MotivoParada(); m.setEmpresa(empresa); apply(m, req, codigo); m.touchForCreate(); return mapper.toResponse(repository.save(m));
    }
    @Transactional(readOnly = true) public Page<MotivoParadaResponse> list(Long empresaId, Boolean ativo, String codigo, String nome, String categoria, Pageable pageable) {
        empresaService.find(empresaId); Page<MotivoParada> page;
        if (codigo != null && !codigo.isBlank()) page = repository.findByEmpresaIdAndCodigoContainingIgnoreCase(empresaId, codigo.trim(), pageable);
        else if (nome != null && !nome.isBlank()) page = repository.findByEmpresaIdAndNomeContainingIgnoreCase(empresaId, nome.trim(), pageable);
        else if (categoria != null && !categoria.isBlank()) page = repository.findByEmpresaIdAndCategoriaContainingIgnoreCase(empresaId, categoria.trim(), pageable);
        else if (ativo != null) page = repository.findByEmpresaIdAndAtivo(empresaId, ativo, pageable); else page = repository.findByEmpresaId(empresaId, pageable);
        return page.map(mapper::toResponse);
    }
    @Transactional(readOnly = true) public MotivoParada find(Long empresaId, Long id) { empresaService.find(empresaId); return repository.findByIdAndEmpresaId(id, empresaId).orElseThrow(() -> new ResourceNotFoundException("Motivo de parada não encontrado nesta empresa.")); }
    @Transactional(readOnly = true) public MotivoParadaResponse get(Long empresaId, Long id) { return mapper.toResponse(find(empresaId, id)); }
    @Transactional public MotivoParadaResponse update(Long empresaId, Long id, MotivoParadaRequest req) { var m = find(empresaId, id); var codigo = text(req.codigo()).toUpperCase(java.util.Locale.ROOT); ensureUnique(empresaId, codigo, id); apply(m, req, codigo); m.touchForUpdate(); return mapper.toResponse(repository.save(m)); }
    @Transactional public MotivoParadaResponse setActive(Long empresaId, Long id, boolean active) { var m = find(empresaId, id); m.setAtivo(active); m.touchForUpdate(); return mapper.toResponse(repository.save(m)); }
    private void ensureUnique(Long empresaId, String codigo, Long id) { boolean exists = id == null ? repository.existsByEmpresaIdAndCodigo(empresaId, codigo) : repository.existsByEmpresaIdAndCodigoAndIdNot(empresaId, codigo, id); if (exists) throw new DuplicateResourceException("Já existe um motivo com o código " + codigo + " nesta empresa."); }
    private static void apply(MotivoParada m, MotivoParadaRequest r, String codigo) { m.setCodigo(codigo); m.setNome(text(r.nome())); m.setDescricao(nullable(r.descricao())); m.setCategoria(nullable(r.categoria())); }
    private static String text(String v) { return EmpresaService.normalizeText(v); } private static String nullable(String v) { return v == null ? null : text(v); }
}
