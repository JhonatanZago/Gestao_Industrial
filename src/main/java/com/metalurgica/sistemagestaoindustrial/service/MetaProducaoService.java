package com.metalurgica.sistemagestaoindustrial.service;

import com.metalurgica.sistemagestaoindustrial.domain.*;
import com.metalurgica.sistemagestaoindustrial.audit.*;
import com.metalurgica.sistemagestaoindustrial.dto.request.MetaProducaoRequest;
import com.metalurgica.sistemagestaoindustrial.dto.response.MetaProducaoResponse;
import com.metalurgica.sistemagestaoindustrial.exception.DuplicateResourceException;
import com.metalurgica.sistemagestaoindustrial.exception.ResourceNotFoundException;
import com.metalurgica.sistemagestaoindustrial.mapper.MetaProducaoMapper;
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
public class MetaProducaoService {
    private final MetaProducaoRepository repository; private final EmpresaService empresaService;
    private final SetorRepository setorRepository; private final FuncionarioRepository funcionarioRepository;
    private final PecaRepository pecaRepository; private final MaquinaRepository maquinaRepository;
    private final TurnoRepository turnoRepository; private final MetaProducaoMapper mapper;
    private final SectorScopeService sectorScopeService;
    private final AuditoriaService auditoriaService;

    public MetaProducaoService(MetaProducaoRepository repository, EmpresaService empresaService, SetorRepository setorRepository,
                               FuncionarioRepository funcionarioRepository, PecaRepository pecaRepository,
                               MaquinaRepository maquinaRepository, TurnoRepository turnoRepository, MetaProducaoMapper mapper, SectorScopeService sectorScopeService, AuditoriaService auditoriaService) {
        this.repository = repository; this.empresaService = empresaService; this.setorRepository = setorRepository;
        this.funcionarioRepository = funcionarioRepository; this.pecaRepository = pecaRepository;
        this.maquinaRepository = maquinaRepository; this.turnoRepository = turnoRepository; this.mapper = mapper; this.sectorScopeService = sectorScopeService; this.auditoriaService = auditoriaService;
    }

    @Transactional public MetaProducaoResponse create(Long e, MetaProducaoRequest r) { sectorScopeService.assertAllowed(e, r.setorId()); var m = new MetaProducao(); m.setEmpresa(empresaService.find(e)); apply(m, e, r, null); m.touchForCreate(); var saved = repository.save(m); auditoriaService.record(e, TipoRecursoAuditoria.META_PRODUCAO, saved.getId(), AcaoAuditoria.CRIACAO, OrigemAuditoria.MANUAL, null, snapshot(saved), metadata(saved), null); return mapper.toResponse(saved); }
    @Transactional(readOnly = true) public Page<MetaProducaoResponse> list(Long e, TipoMeta tipo, Boolean ativo, Pageable p) { empresaService.find(e); Specification<MetaProducao> spec = (root, query, cb) -> cb.and(cb.equal(root.get("empresa").get("id"), e), sectorScopeService.restrictIncludingGlobal(root, cb, e, "setor")); if (tipo != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("tipo"), tipo)); if (ativo != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("ativo"), ativo)); return repository.findAll(spec, p).map(mapper::toResponse); }
    @Transactional(readOnly = true) public MetaProducao find(Long e, Long id) { empresaService.find(e); return repository.findByIdAndEmpresaId(id, e).orElseThrow(() -> new ResourceNotFoundException("Meta de produção não encontrada nesta empresa.")); }
    @Transactional(readOnly = true) public MetaProducaoResponse get(Long e, Long id) { var found = find(e, id); if (found.getSetor() != null) sectorScopeService.assertAllowed(e, found.getSetor().getId()); return mapper.toResponse(found); }
    @Transactional public MetaProducaoResponse update(Long e, Long id, MetaProducaoRequest r) { sectorScopeService.assertAllowed(e, r.setorId()); var m = find(e, id); var before = snapshot(m); apply(m, e, r, id); m.touchForUpdate(); var saved = repository.save(m); auditoriaService.record(e, TipoRecursoAuditoria.META_PRODUCAO, id, AcaoAuditoria.ALTERACAO, OrigemAuditoria.MANUAL, before, snapshot(saved), metadata(saved), null); return mapper.toResponse(saved); }
    @Transactional public MetaProducaoResponse setActive(Long e, Long id, boolean active) { var m = find(e, id); var before = snapshot(m); m.setAtivo(active); m.touchForUpdate(); var saved = repository.save(m); auditoriaService.record(e, TipoRecursoAuditoria.META_PRODUCAO, id, active ? AcaoAuditoria.ATIVACAO : AcaoAuditoria.DESATIVACAO, OrigemAuditoria.MANUAL, before, snapshot(saved), metadata(saved), null); return mapper.toResponse(saved); }

    private void apply(MetaProducao m, Long e, MetaProducaoRequest r, Long id) {
        sectorScopeService.assertAllowed(e, r.setorId());
        if (r.vigenciaFim() != null && r.vigenciaFim().isBefore(r.vigenciaInicio())) throw new IllegalArgumentException("A vigência final não pode anteceder a inicial.");
        var setor = r.setorId() == null ? null : setorRepository.findByIdAndEmpresaId(r.setorId(), e).orElseThrow(() -> new ResourceNotFoundException("Setor não encontrado nesta empresa."));
        var funcionario = r.funcionarioId() == null ? null : funcionarioRepository.findByIdAndEmpresaId(r.funcionarioId(), e).orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado nesta empresa."));
        var peca = r.pecaId() == null ? null : pecaRepository.findByIdAndEmpresaId(r.pecaId(), e).orElseThrow(() -> new ResourceNotFoundException("Peça não encontrada nesta empresa."));
        var maquina = r.maquinaId() == null ? null : maquinaRepository.findByIdAndEmpresaId(r.maquinaId(), e).orElseThrow(() -> new ResourceNotFoundException("Máquina não encontrada nesta empresa."));
        var turno = r.turnoId() == null ? null : turnoRepository.findByIdAndEmpresaId(r.turnoId(), e).orElseThrow(() -> new ResourceNotFoundException("Turno não encontrado nesta empresa."));
        validateContext(r.tipo(), setor, funcionario, peca, maquina, turno);
        if (repository.existsConflict(e, r.tipo(), r.periodicidade(), r.setorId(), r.funcionarioId(), r.pecaId(), r.maquinaId(), r.turnoId(), r.vigenciaInicio(), r.vigenciaFim(), id)) throw new DuplicateResourceException("Já existe meta ativa com vigência sobreposta neste contexto.");
        m.setNome(r.nome().trim()); m.setDescricao(r.descricao()); m.setTipo(r.tipo()); m.setSetor(setor); m.setFuncionario(funcionario); m.setPeca(peca); m.setMaquina(maquina); m.setTurno(turno); m.setQuantidadeMeta(r.quantidadeMeta()); m.setPeriodicidade(r.periodicidade()); m.setVigenciaInicio(r.vigenciaInicio()); m.setVigenciaFim(r.vigenciaFim());
    }

    private static void validateContext(TipoMeta tipo, Setor setor, Funcionario funcionario, Peca peca, Maquina maquina, Turno turno) {
        int count = (setor == null ? 0 : 1) + (funcionario == null ? 0 : 1) + (peca == null ? 0 : 1) + (maquina == null ? 0 : 1) + (turno == null ? 0 : 1);
        switch (tipo) {
            case EMPRESA -> { if (count != 0) throw new IllegalArgumentException("Meta de empresa não aceita contexto adicional."); }
            case SETOR -> { if (setor == null || count != 1) throw new IllegalArgumentException("Meta de setor exige somente um setor."); }
            case FUNCIONARIO -> { if (funcionario == null || count != 1) throw new IllegalArgumentException("Meta de funcionário exige somente um funcionário."); }
            case PECA -> { if (peca == null || count != 1) throw new IllegalArgumentException("Meta de peça exige somente uma peça."); }
            case MAQUINA -> { if (maquina == null || count != 1) throw new IllegalArgumentException("Meta de máquina exige somente uma máquina."); }
            case TURNO -> { if (turno == null || count != 1) throw new IllegalArgumentException("Meta de turno exige somente um turno."); }
            case COMPOSTA -> { if (count < 2) throw new IllegalArgumentException("Meta composta exige pelo menos dois contextos."); }
        }
    }

    private static Map<String, Object> snapshot(MetaProducao m) { var v = new LinkedHashMap<String, Object>(); v.put("id", m.getId()); v.put("nome", m.getNome()); v.put("descricao", m.getDescricao()); v.put("tipo", m.getTipo()); v.put("setorId", m.getSetor() == null ? null : m.getSetor().getId()); v.put("funcionarioId", m.getFuncionario() == null ? null : m.getFuncionario().getId()); v.put("pecaId", m.getPeca() == null ? null : m.getPeca().getId()); v.put("maquinaId", m.getMaquina() == null ? null : m.getMaquina().getId()); v.put("turnoId", m.getTurno() == null ? null : m.getTurno().getId()); v.put("quantidadeAlvo", m.getQuantidadeMeta()); v.put("periodicidade", m.getPeriodicidade()); v.put("vigenciaInicio", m.getVigenciaInicio()); v.put("vigenciaFim", m.getVigenciaFim()); v.put("ativo", m.isAtivo()); return v; }
    private static Map<String, Object> metadata(MetaProducao m) { return m.getSetor() == null ? Map.of() : Map.of("setorId", m.getSetor().getId()); }
}
