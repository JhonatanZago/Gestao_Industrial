package com.metalurgica.sistemagestaoindustrial.service;

import com.metalurgica.sistemagestaoindustrial.domain.Setor;
import com.metalurgica.sistemagestaoindustrial.dto.request.SetorRequest;
import com.metalurgica.sistemagestaoindustrial.dto.response.SetorResponse;
import com.metalurgica.sistemagestaoindustrial.exception.DuplicateResourceException;
import com.metalurgica.sistemagestaoindustrial.exception.ResourceNotFoundException;
import com.metalurgica.sistemagestaoindustrial.mapper.SetorMapper;
import com.metalurgica.sistemagestaoindustrial.repository.SetorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SetorService {
    private final SetorRepository repository;
    private final EmpresaService empresaService;
    private final SetorMapper mapper;

    public SetorService(SetorRepository repository, EmpresaService empresaService, SetorMapper mapper) {
        this.repository = repository; this.empresaService = empresaService; this.mapper = mapper;
    }

    @Transactional
    public SetorResponse create(Long empresaId, SetorRequest request) {
        var empresa = empresaService.find(empresaId);
        var codigo = normalizeCode(request.codigo()); var nome = EmpresaService.normalizeText(request.nome());
        ensureUnique(empresaId, codigo, nome, null);
        var s = new Setor(); s.setEmpresa(empresa); s.setCodigo(codigo); s.setNome(nome);
        s.setNomeNormalizado(normalizeName(nome)); s.setDescricao(normalizeNullable(request.descricao())); s.touchForCreate();
        return mapper.toResponse(repository.save(s));
    }

    @Transactional(readOnly = true)
    public Page<SetorResponse> list(Long empresaId, Boolean ativo, String nome, String codigo, Pageable pageable) {
        empresaService.find(empresaId);
        Page<Setor> page;
        if (nome != null && !nome.isBlank()) page = repository.findByEmpresaIdAndNomeContainingIgnoreCase(empresaId, nome.trim(), pageable);
        else if (codigo != null && !codigo.isBlank()) page = repository.findByEmpresaIdAndCodigoContainingIgnoreCase(empresaId, codigo.trim(), pageable);
        else if (ativo != null) page = repository.findByEmpresaIdAndAtivo(empresaId, ativo, pageable);
        else page = repository.findByEmpresaId(empresaId, pageable);
        return page.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Setor find(Long empresaId, Long setorId) {
        empresaService.find(empresaId);
        return repository.findByIdAndEmpresaId(setorId, empresaId).orElseThrow(() -> new ResourceNotFoundException("Setor não encontrado nesta empresa."));
    }

    @Transactional(readOnly = true)
    public SetorResponse get(Long empresaId, Long setorId) { return mapper.toResponse(find(empresaId, setorId)); }

    @Transactional
    public SetorResponse update(Long empresaId, Long setorId, SetorRequest request) {
        var s = find(empresaId, setorId); var codigo = normalizeCode(request.codigo()); var nome = EmpresaService.normalizeText(request.nome());
        ensureUnique(empresaId, codigo, nome, setorId);
        s.setCodigo(codigo); s.setNome(nome); s.setNomeNormalizado(normalizeName(nome)); s.setDescricao(normalizeNullable(request.descricao())); s.touchForUpdate();
        return mapper.toResponse(repository.save(s));
    }

    @Transactional
    public SetorResponse setActive(Long empresaId, Long setorId, boolean active) {
        var s = find(empresaId, setorId); s.setAtivo(active); s.touchForUpdate(); return mapper.toResponse(repository.save(s));
    }

    private void ensureUnique(Long empresaId, String codigo, String nome, Long id) {
        if (id == null ? repository.existsByEmpresaIdAndCodigo(empresaId, codigo) : repository.existsByEmpresaIdAndCodigoAndIdNot(empresaId, codigo, id))
            throw new DuplicateResourceException("Já existe um setor com o código " + codigo + " nesta empresa.");
        var nomeNormalizado = normalizeName(nome);
        if (id == null ? repository.existsByEmpresaIdAndNomeNormalizado(empresaId, nomeNormalizado) : repository.existsByEmpresaIdAndNomeNormalizadoAndIdNot(empresaId, nomeNormalizado, id))
            throw new DuplicateResourceException("Já existe um setor com este nome nesta empresa.");
    }

    public static String normalizeCode(String value) { return EmpresaService.normalizeText(value).toUpperCase(java.util.Locale.ROOT); }
    public static String normalizeName(String value) { return EmpresaService.normalizeText(value).toLowerCase(java.util.Locale.ROOT); }
    private static String normalizeNullable(String value) { return value == null ? null : EmpresaService.normalizeText(value); }
}
