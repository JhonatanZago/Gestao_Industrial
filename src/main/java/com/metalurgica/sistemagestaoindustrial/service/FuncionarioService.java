package com.metalurgica.sistemagestaoindustrial.service;

import com.metalurgica.sistemagestaoindustrial.domain.Funcionario;
import com.metalurgica.sistemagestaoindustrial.dto.request.FuncionarioRequest;
import com.metalurgica.sistemagestaoindustrial.dto.response.FuncionarioResponse;
import com.metalurgica.sistemagestaoindustrial.exception.DuplicateResourceException;
import com.metalurgica.sistemagestaoindustrial.exception.ResourceNotFoundException;
import com.metalurgica.sistemagestaoindustrial.mapper.FuncionarioMapper;
import com.metalurgica.sistemagestaoindustrial.repository.FuncionarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FuncionarioService {
    private final FuncionarioRepository repository;
    private final EmpresaService empresaService;
    private final FuncionarioMapper mapper;

    public FuncionarioService(FuncionarioRepository repository, EmpresaService empresaService, FuncionarioMapper mapper) {
        this.repository = repository; this.empresaService = empresaService; this.mapper = mapper;
    }

    @Transactional
    public FuncionarioResponse create(Long empresaId, FuncionarioRequest request) {
        var empresa = empresaService.find(empresaId); var codigo = normalize(request.codigo());
        ensureUnique(empresaId, codigo, null);
        var f = new Funcionario(); f.setEmpresa(empresa); f.setCodigo(codigo); f.setNome(EmpresaService.normalizeText(request.nome())); f.touchForCreate();
        return mapper.toResponse(repository.save(f));
    }

    @Transactional(readOnly = true)
    public Page<FuncionarioResponse> list(Long empresaId, Boolean ativo, String codigo, String nome, Pageable pageable) {
        empresaService.find(empresaId);
        Page<Funcionario> page;
        if (codigo != null && !codigo.isBlank()) page = repository.findByEmpresaIdAndCodigoContainingIgnoreCase(empresaId, codigo.trim(), pageable);
        else if (nome != null && !nome.isBlank()) page = repository.findByEmpresaIdAndNomeContainingIgnoreCase(empresaId, nome.trim(), pageable);
        else if (ativo != null) page = repository.findByEmpresaIdAndAtivo(empresaId, ativo, pageable);
        else page = repository.findByEmpresaId(empresaId, pageable);
        return page.map(mapper::toResponse);
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
        f.setCodigo(codigo); f.setNome(EmpresaService.normalizeText(request.nome())); f.touchForUpdate(); return mapper.toResponse(repository.save(f));
    }

    @Transactional
    public FuncionarioResponse setActive(Long empresaId, Long funcionarioId, boolean active) {
        var f = find(empresaId, funcionarioId); f.setAtivo(active); f.touchForUpdate(); return mapper.toResponse(repository.save(f));
    }

    private void ensureUnique(Long empresaId, String codigo, Long id) {
        boolean exists = id == null ? repository.existsByEmpresaIdAndCodigo(empresaId, codigo) : repository.existsByEmpresaIdAndCodigoAndIdNot(empresaId, codigo, id);
        if (exists) throw new DuplicateResourceException("Já existe um funcionário com o código " + codigo + " nesta empresa.");
    }

    private static String normalize(String value) { return EmpresaService.normalizeText(value); }
}
