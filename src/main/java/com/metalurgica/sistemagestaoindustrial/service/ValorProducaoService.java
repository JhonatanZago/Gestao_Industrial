package com.metalurgica.sistemagestaoindustrial.service;

import com.metalurgica.sistemagestaoindustrial.domain.*;
import com.metalurgica.sistemagestaoindustrial.audit.*;
import com.metalurgica.sistemagestaoindustrial.dto.request.ValorProducaoRequest;
import com.metalurgica.sistemagestaoindustrial.dto.response.ValorProducaoResponse;
import com.metalurgica.sistemagestaoindustrial.exception.*;
import com.metalurgica.sistemagestaoindustrial.mapper.ValorProducaoMapper;
import com.metalurgica.sistemagestaoindustrial.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import com.metalurgica.sistemagestaoindustrial.security.SectorScopeService;

import java.time.LocalDate;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ValorProducaoService {
    private final ValorProducaoRepository repository; private final EmpresaService empresaService;
    private final PecaRepository pecaRepository; private final SetorRepository setorRepository;
    private final MaquinaRepository maquinaRepository; private final ValorProducaoMapper mapper;
    private final SectorScopeService sectorScopeService;
    private final AuditoriaService auditoriaService;

    public ValorProducaoService(ValorProducaoRepository repository, EmpresaService empresaService, PecaRepository pecaRepository,
                                SetorRepository setorRepository, MaquinaRepository maquinaRepository, ValorProducaoMapper mapper, SectorScopeService sectorScopeService, AuditoriaService auditoriaService) {
        this.repository = repository; this.empresaService = empresaService; this.pecaRepository = pecaRepository;
        this.setorRepository = setorRepository; this.maquinaRepository = maquinaRepository; this.mapper = mapper; this.sectorScopeService = sectorScopeService; this.auditoriaService = auditoriaService;
    }

    @Transactional public ValorProducaoResponse create(Long e, ValorProducaoRequest r) { var v = new ValorProducao(); v.setEmpresa(empresaService.find(e)); apply(v, e, r, null); v.touchForCreate(); var saved = repository.save(v); auditoriaService.record(e, TipoRecursoAuditoria.VALOR_PRODUCAO, saved.getId(), AcaoAuditoria.CRIACAO, OrigemAuditoria.MANUAL, null, snapshot(saved), Map.of("setorId", saved.getSetor().getId()), null); return mapper.toResponse(saved); }
    @Transactional(readOnly = true)
    public Page<ValorProducaoResponse> list(Long e, Long pecaId, Long setorId, Long maquinaId, TipoValor tipo, String moeda, Boolean ativo, LocalDate vigenteEm, Pageable p) {
        empresaService.find(e); sectorScopeService.assertAllowed(e, setorId); Specification<ValorProducao> scoped = (root, query, cb) -> cb.and(cb.equal(root.get("empresa").get("id"), e), sectorScopeService.restrict(root, cb, e, "setor"));
        if (pecaId != null) scoped = scoped.and((root, query, cb) -> cb.equal(root.get("peca").get("id"), pecaId));
        if (setorId != null) scoped = scoped.and((root, query, cb) -> cb.equal(root.get("setor").get("id"), setorId));
        if (maquinaId != null) scoped = scoped.and((root, query, cb) -> cb.equal(root.get("maquina").get("id"), maquinaId));
        if (tipo != null) scoped = scoped.and((root, query, cb) -> cb.equal(root.get("tipoValor"), tipo));
        if (moeda != null) scoped = scoped.and((root, query, cb) -> cb.equal(root.get("moeda"), moeda.trim().toUpperCase()));
        if (ativo != null) scoped = scoped.and((root, query, cb) -> cb.equal(root.get("ativo"), ativo));
        if (vigenteEm != null) scoped = scoped.and((root, query, cb) -> cb.and(cb.lessThanOrEqualTo(root.get("vigenciaInicio"), vigenteEm), cb.or(cb.isNull(root.get("vigenciaFim")), cb.greaterThanOrEqualTo(root.get("vigenciaFim"), vigenteEm))));
        return repository.findAll(scoped, p).map(mapper::toResponse);
        /* Page<ValorProducao> result;
        if (pecaId != null && setorId == null && maquinaId == null && tipo == null && moeda == null && ativo == null && vigenteEm == null) result = repository.findByEmpresaIdAndPecaId(e, pecaId, p);
        else if (setorId != null && pecaId == null && maquinaId == null && tipo == null && moeda == null && ativo == null && vigenteEm == null) result = repository.findByEmpresaIdAndSetorId(e, setorId, p);
        else if (maquinaId != null && pecaId == null && setorId == null && tipo == null && moeda == null && ativo == null && vigenteEm == null) result = repository.findByEmpresaIdAndMaquinaId(e, maquinaId, p);
        else if (tipo != null && pecaId == null && setorId == null && maquinaId == null && moeda == null && ativo == null && vigenteEm == null) result = repository.findByEmpresaIdAndTipoValor(e, tipo, p);
        else if (moeda != null && pecaId == null && setorId == null && maquinaId == null && tipo == null && ativo == null && vigenteEm == null) result = repository.findByEmpresaIdAndMoeda(e, moeda.trim().toUpperCase(), p);
        else if (ativo != null && pecaId == null && setorId == null && maquinaId == null && tipo == null && moeda == null && vigenteEm == null) result = repository.findByEmpresaIdAndAtivo(e, ativo, p);
        else result = repository.findByEmpresaId(e, p);
        return result.map(mapper::toResponse); */
    }
    @Transactional(readOnly = true) public ValorProducao find(Long e, Long id) { empresaService.find(e); return repository.findByIdAndEmpresaId(id, e).orElseThrow(() -> new ResourceNotFoundException("Valor de produção não encontrado nesta empresa.")); }
    @Transactional(readOnly = true) public ValorProducaoResponse get(Long e, Long id) { var found = find(e, id); sectorScopeService.assertAllowed(e, found.getSetor().getId()); return mapper.toResponse(found); }
    @Transactional public ValorProducaoResponse update(Long e, Long id, ValorProducaoRequest r) { sectorScopeService.assertAllowed(e, r.setorId()); var v = find(e, id); var before = snapshot(v); apply(v, e, r, id); v.touchForUpdate(); var saved = repository.save(v); auditoriaService.record(e, TipoRecursoAuditoria.VALOR_PRODUCAO, id, AcaoAuditoria.ALTERACAO, OrigemAuditoria.MANUAL, before, snapshot(saved), Map.of("setorId", saved.getSetor().getId()), null); return mapper.toResponse(saved); }
    @Transactional public ValorProducaoResponse setActive(Long e, Long id, boolean active) { var v = find(e, id); var before = snapshot(v); v.setAtivo(active); v.touchForUpdate(); var saved = repository.save(v); auditoriaService.record(e, TipoRecursoAuditoria.VALOR_PRODUCAO, id, active ? AcaoAuditoria.ATIVACAO : AcaoAuditoria.DESATIVACAO, OrigemAuditoria.MANUAL, before, snapshot(saved), Map.of("setorId", saved.getSetor().getId()), null); return mapper.toResponse(saved); }

    private void apply(ValorProducao v, Long e, ValorProducaoRequest r, Long id) {
        sectorScopeService.assertAllowed(e, r.setorId());
        if (r.vigenciaFim() != null && r.vigenciaFim().isBefore(r.vigenciaInicio())) throw new IllegalArgumentException("A vigência final não pode anteceder a inicial.");
        String moeda = r.moeda().trim().toUpperCase();
        try { if (!moeda.matches("[A-Z]{3}")) throw new IllegalArgumentException(); Currency.getInstance(moeda); } catch (IllegalArgumentException ex) { throw new IllegalArgumentException("A moeda informada é inválida."); }
        var peca = pecaRepository.findByIdAndEmpresaId(r.pecaId(), e).orElseThrow(() -> new ResourceNotFoundException("A peça informada não pertence à empresa."));
        var setor = setorRepository.findByIdAndEmpresaId(r.setorId(), e).orElseThrow(() -> new ResourceNotFoundException("O setor informado não pertence à empresa."));
        Maquina maquina = r.maquinaId() == null ? null : maquinaRepository.findByIdAndEmpresaId(r.maquinaId(), e).orElseThrow(() -> new ResourceNotFoundException("A máquina informada não pertence à empresa."));
        if (maquina != null && !maquina.getSetor().getId().equals(setor.getId())) throw new BusinessException("A máquina deve pertencer ao setor informado.");
        if (repository.existsConflict(e, r.pecaId(), r.setorId(), r.maquinaId(), r.tipoValor(), moeda, r.vigenciaInicio(), r.vigenciaFim(), id)) throw new DuplicateResourceException("Já existe um valor de produção com vigência sobreposta para este contexto.");
        v.setPeca(peca); v.setSetor(setor); v.setMaquina(maquina); v.setTipoValor(r.tipoValor()); v.setValorUnitario(r.valorUnitario()); v.setMoeda(moeda); v.setVigenciaInicio(r.vigenciaInicio()); v.setVigenciaFim(r.vigenciaFim());
    }
    private static Map<String, Object> snapshot(ValorProducao v) { var x = new LinkedHashMap<String, Object>(); x.put("id", v.getId()); x.put("pecaId", v.getPeca().getId()); x.put("setorId", v.getSetor().getId()); x.put("maquinaId", v.getMaquina() == null ? null : v.getMaquina().getId()); x.put("tipoValor", v.getTipoValor()); x.put("valor", v.getValorUnitario()); x.put("moeda", v.getMoeda()); x.put("vigenciaInicio", v.getVigenciaInicio()); x.put("vigenciaFim", v.getVigenciaFim()); x.put("ativo", v.isAtivo()); return x; }
}
