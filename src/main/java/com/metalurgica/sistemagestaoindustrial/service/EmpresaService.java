package com.metalurgica.sistemagestaoindustrial.service;

import com.metalurgica.sistemagestaoindustrial.domain.Empresa;
import com.metalurgica.sistemagestaoindustrial.dto.request.EmpresaRequest;
import com.metalurgica.sistemagestaoindustrial.dto.response.EmpresaResponse;
import com.metalurgica.sistemagestaoindustrial.exception.DuplicateResourceException;
import com.metalurgica.sistemagestaoindustrial.exception.ResourceNotFoundException;
import com.metalurgica.sistemagestaoindustrial.mapper.EmpresaMapper;
import com.metalurgica.sistemagestaoindustrial.repository.EmpresaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpresaService {
    private final EmpresaRepository repository;
    private final EmpresaMapper mapper;

    public EmpresaService(EmpresaRepository repository, EmpresaMapper mapper) { this.repository = repository; this.mapper = mapper; }

    @Transactional
    public EmpresaResponse create(EmpresaRequest request) {
        var cnpj = normalizeCnpj(request.cnpj());
        if (repository.existsByCnpj(cnpj)) throw new DuplicateResourceException("Já existe uma empresa com este CNPJ.");
        var e = new Empresa();
        e.setRazaoSocial(normalizeText(request.razaoSocial()));
        e.setNomeFantasia(normalizeText(request.nomeFantasia()));
        e.setCnpj(cnpj);
        e.touchForCreate();
        return mapper.toResponse(repository.save(e));
    }

    @Transactional(readOnly = true)
    public Page<EmpresaResponse> list(Boolean ativo, Pageable pageable) {
        var page = ativo == null ? repository.findAll(pageable) : repository.findByAtivo(ativo, pageable);
        return page.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Empresa find(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada.")); }

    @Transactional(readOnly = true)
    public EmpresaResponse get(Long id) { return mapper.toResponse(find(id)); }

    @Transactional
    public EmpresaResponse update(Long id, EmpresaRequest request) {
        var e = find(id);
        var cnpj = normalizeCnpj(request.cnpj());
        if (repository.findByCnpj(cnpj).filter(other -> !other.getId().equals(id)).isPresent())
            throw new DuplicateResourceException("Já existe uma empresa com este CNPJ.");
        e.setRazaoSocial(normalizeText(request.razaoSocial()));
        e.setNomeFantasia(normalizeText(request.nomeFantasia()));
        e.setCnpj(cnpj);
        e.touchForUpdate();
        return mapper.toResponse(repository.save(e));
    }

    @Transactional
    public EmpresaResponse setActive(Long id, boolean active) {
        var e = find(id); e.setAtivo(active); e.touchForUpdate(); return mapper.toResponse(repository.save(e));
    }

    public static String normalizeText(String value) { return value == null ? null : value.trim().replaceAll("\\s+", " "); }
    public static String normalizeCnpj(String value) {
        var normalized = value == null ? "" : value.replaceAll("\\D", "");
        if (normalized.length() != 14) throw new IllegalArgumentException("CNPJ deve conter 14 dígitos.");
        return normalized;
    }
}
