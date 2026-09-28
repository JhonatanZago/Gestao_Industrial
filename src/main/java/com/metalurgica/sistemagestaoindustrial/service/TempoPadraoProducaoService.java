package com.metalurgica.sistemagestaoindustrial.service;

import com.metalurgica.sistemagestaoindustrial.domain.*;
import com.metalurgica.sistemagestaoindustrial.audit.*;
import com.metalurgica.sistemagestaoindustrial.dto.request.TempoPadraoProducaoRequest;
import com.metalurgica.sistemagestaoindustrial.dto.response.TempoPadraoProducaoResponse;
import com.metalurgica.sistemagestaoindustrial.exception.BusinessException;
import com.metalurgica.sistemagestaoindustrial.exception.DuplicateResourceException;
import com.metalurgica.sistemagestaoindustrial.exception.ResourceNotFoundException;
import com.metalurgica.sistemagestaoindustrial.mapper.TempoPadraoProducaoMapper;
import com.metalurgica.sistemagestaoindustrial.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import com.metalurgica.sistemagestaoindustrial.security.SectorScopeService;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TempoPadraoProducaoService {
    private final TempoPadraoProducaoRepository repository;
    private final EmpresaService empresaService;
    private final PecaRepository pecaRepository;
    private final SetorRepository setorRepository;
    private final MaquinaRepository maquinaRepository;
    private final TempoPadraoProducaoMapper mapper;
    private final SectorScopeService sectorScopeService;
    private final AuditoriaService auditoriaService;

    public TempoPadraoProducaoService(TempoPadraoProducaoRepository repository, EmpresaService empresaService,
                                      PecaRepository pecaRepository, SetorRepository setorRepository,
                                      MaquinaRepository maquinaRepository, TempoPadraoProducaoMapper mapper, SectorScopeService sectorScopeService, AuditoriaService auditoriaService) {
        this.repository = repository; this.empresaService = empresaService; this.pecaRepository = pecaRepository;
        this.setorRepository = setorRepository; this.maquinaRepository = maquinaRepository; this.mapper = mapper; this.sectorScopeService = sectorScopeService; this.auditoriaService = auditoriaService;
    }

    @Transactional
    public TempoPadraoProducaoResponse create(Long empresaId, TempoPadraoProducaoRequest request) {
        var t = new TempoPadraoProducao(); t.setEmpresa(empresaService.find(empresaId));
        apply(t, empresaId, request, null); t.touchForCreate(); var saved = repository.save(t); auditoriaService.record(empresaId, TipoRecursoAuditoria.TEMPO_PADRAO, saved.getId(), AcaoAuditoria.CRIACAO, OrigemAuditoria.MANUAL, null, snapshot(saved), Map.of("setorId", saved.getSetor().getId()), null); return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<TempoPadraoProducaoResponse> list(Long empresaId, Long pecaId, Long setorId, Long maquinaId,
                                                    Boolean ativo, LocalDate vigenteEm, Pageable pageable) {
        empresaService.find(empresaId);
        sectorScopeService.assertAllowed(empresaId, setorId);
        Specification<TempoPadraoProducao> scoped = (root, query, cb) -> cb.and(cb.equal(root.get("empresa").get("id"), empresaId), sectorScopeService.restrict(root, cb, empresaId, "setor"));
        if (pecaId != null) scoped = scoped.and((root, query, cb) -> cb.equal(root.get("peca").get("id"), pecaId));
        if (setorId != null) scoped = scoped.and((root, query, cb) -> cb.equal(root.get("setor").get("id"), setorId));
        if (maquinaId != null) scoped = scoped.and((root, query, cb) -> cb.equal(root.get("maquina").get("id"), maquinaId));
        if (ativo != null) scoped = scoped.and((root, query, cb) -> cb.equal(root.get("ativo"), ativo));
        if (vigenteEm != null) scoped = scoped.and((root, query, cb) -> cb.and(cb.lessThanOrEqualTo(root.get("vigenciaInicio"), vigenteEm), cb.or(cb.isNull(root.get("vigenciaFim")), cb.greaterThanOrEqualTo(root.get("vigenciaFim"), vigenteEm))));
        return repository.findAll(scoped, pageable).map(mapper::toResponse);
        /*
        if (pecaId != null && setorId == null && maquinaId == null && ativo == null && vigenteEm == null)
            return repository.findByEmpresaIdAndPecaId(empresaId, pecaId, pageable).map(mapper::toResponse);
        if (setorId != null && pecaId == null && maquinaId == null && ativo == null && vigenteEm == null)
            return repository.findByEmpresaIdAndSetorId(empresaId, setorId, pageable).map(mapper::toResponse);
        if (maquinaId != null && pecaId == null && setorId == null && ativo == null && vigenteEm == null)
            return repository.findByEmpresaIdAndMaquinaId(empresaId, maquinaId, pageable).map(mapper::toResponse);
        if (ativo != null && pecaId == null && setorId == null && maquinaId == null && vigenteEm == null)
            return repository.findByEmpresaIdAndAtivo(empresaId, ativo, pageable).map(mapper::toResponse);
        return repository.findByEmpresaId(empresaId, pageable).map(mapper::toResponse); */
    }

    @Transactional(readOnly = true)
    public TempoPadraoProducao find(Long empresaId, Long id) {
        empresaService.find(empresaId);
        return repository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Tempo padrão não encontrado nesta empresa."));
    }

    @Transactional(readOnly = true) public TempoPadraoProducaoResponse get(Long e, Long id) { var found = find(e, id); sectorScopeService.assertAllowed(e, found.getSetor().getId()); return mapper.toResponse(found); }

    @Transactional
    public TempoPadraoProducaoResponse update(Long empresaId, Long id, TempoPadraoProducaoRequest request) {
        sectorScopeService.assertAllowed(empresaId, request.setorId());
        var t = find(empresaId, id); var before = snapshot(t); apply(t, empresaId, request, id); t.touchForUpdate(); var saved = repository.save(t); auditoriaService.record(empresaId, TipoRecursoAuditoria.TEMPO_PADRAO, id, AcaoAuditoria.ALTERACAO, OrigemAuditoria.MANUAL, before, snapshot(saved), Map.of("setorId", saved.getSetor().getId()), null); return mapper.toResponse(saved);
    }

    @Transactional
    public TempoPadraoProducaoResponse setActive(Long empresaId, Long id, boolean active) {
        var t = find(empresaId, id); var before = snapshot(t); t.setAtivo(active); t.touchForUpdate(); var saved = repository.save(t); auditoriaService.record(empresaId, TipoRecursoAuditoria.TEMPO_PADRAO, id, active ? AcaoAuditoria.ATIVACAO : AcaoAuditoria.DESATIVACAO, OrigemAuditoria.MANUAL, before, snapshot(saved), Map.of("setorId", saved.getSetor().getId()), null); return mapper.toResponse(saved);
    }

    private void apply(TempoPadraoProducao t, Long empresaId, TempoPadraoProducaoRequest r, Long id) {
        sectorScopeService.assertAllowed(empresaId, r.setorId());
        if (r.vigenciaFim() != null && r.vigenciaFim().isBefore(r.vigenciaInicio())) throw new IllegalArgumentException("A vigência final não pode anteceder a inicial.");
        if (r.baseTempo() == BaseTempoPadrao.POR_LOTE && r.quantidadeReferencia() == null) throw new IllegalArgumentException("Quantidade de referência é obrigatória para base por lote.");
        if (r.baseTempo() == BaseTempoPadrao.POR_UNIDADE && r.quantidadeReferencia() != null) throw new IllegalArgumentException("Quantidade de referência não se aplica à base por unidade.");
        var peca = pecaRepository.findByIdAndEmpresaId(r.pecaId(), empresaId).orElseThrow(() -> new ResourceNotFoundException("Peça não encontrada nesta empresa."));
        var setor = setorRepository.findByIdAndEmpresaId(r.setorId(), empresaId).orElseThrow(() -> new ResourceNotFoundException("Setor não encontrado nesta empresa."));
        Maquina maquina = r.maquinaId() == null ? null : maquinaRepository.findByIdAndEmpresaId(r.maquinaId(), empresaId).orElseThrow(() -> new ResourceNotFoundException("Máquina não encontrada nesta empresa."));
        if (maquina != null && !maquina.getSetor().getId().equals(setor.getId())) throw new BusinessException("A máquina deve pertencer ao setor informado.");
        if (repository.existsConflict(empresaId, r.pecaId(), r.setorId(), r.maquinaId(), r.vigenciaInicio(), r.vigenciaFim(), id)) throw new DuplicateResourceException("Já existe tempo padrão ativo com vigência sobreposta neste contexto.");
        t.setPeca(peca); t.setSetor(setor); t.setMaquina(maquina); t.setSegundos(r.segundos()); t.setBaseTempo(r.baseTempo()); t.setQuantidadeReferencia(r.quantidadeReferencia()); t.setVigenciaInicio(r.vigenciaInicio()); t.setVigenciaFim(r.vigenciaFim());
    }

    private static Map<String, Object> snapshot(TempoPadraoProducao t) { var v = new LinkedHashMap<String, Object>(); v.put("id", t.getId()); v.put("pecaId", t.getPeca().getId()); v.put("setorId", t.getSetor().getId()); v.put("maquinaId", t.getMaquina() == null ? null : t.getMaquina().getId()); v.put("segundos", t.getSegundos()); v.put("baseTempo", t.getBaseTempo()); v.put("quantidadeReferencia", t.getQuantidadeReferencia()); v.put("vigenciaInicio", t.getVigenciaInicio()); v.put("vigenciaFim", t.getVigenciaFim()); v.put("ativo", t.isAtivo()); return v; }
}
