package com.metalurgica.sistemagestaoindustrial.service;

import com.metalurgica.sistemagestaoindustrial.domain.*;
import com.metalurgica.sistemagestaoindustrial.audit.*;
import com.metalurgica.sistemagestaoindustrial.dto.request.RegistroProducaoRequest;
import com.metalurgica.sistemagestaoindustrial.dto.response.RegistroProducaoResponse;
import com.metalurgica.sistemagestaoindustrial.exception.BusinessException;
import com.metalurgica.sistemagestaoindustrial.exception.ResourceNotFoundException;
import com.metalurgica.sistemagestaoindustrial.mapper.RegistroProducaoMapper;
import com.metalurgica.sistemagestaoindustrial.repository.*;
import com.metalurgica.sistemagestaoindustrial.security.SectorScopeService;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class RegistroProducaoService {
    private final RegistroProducaoRepository repository;
    private final EmpresaService empresaService;
    private final SetorRepository setorRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PecaRepository pecaRepository;
    private final com.metalurgica.sistemagestaoindustrial.repository.ParadaProducaoRepository paradaRepository;
    private final MaquinaRepository maquinaRepository;
    private final TurnoRepository turnoRepository;
    private final RegistroProducaoMapper mapper;
    private final SectorScopeService sectorScopeService;
    private final AuditoriaService auditoriaService;

    public RegistroProducaoService(RegistroProducaoRepository repository, EmpresaService empresaService,
                                   SetorRepository setorRepository, FuncionarioRepository funcionarioRepository,
                                   PecaRepository pecaRepository, RegistroProducaoMapper mapper,
                                   com.metalurgica.sistemagestaoindustrial.repository.ParadaProducaoRepository paradaRepository,
                                   MaquinaRepository maquinaRepository, TurnoRepository turnoRepository,
                                   SectorScopeService sectorScopeService, AuditoriaService auditoriaService) {
        this.repository = repository; this.empresaService = empresaService; this.setorRepository = setorRepository;
        this.funcionarioRepository = funcionarioRepository; this.pecaRepository = pecaRepository; this.mapper = mapper; this.paradaRepository = paradaRepository; this.maquinaRepository = maquinaRepository; this.turnoRepository = turnoRepository;
        this.sectorScopeService = sectorScopeService; this.auditoriaService = auditoriaService;
    }

    @Transactional
    public RegistroProducaoResponse create(Long empresaId, RegistroProducaoRequest request) {
        var empresa = empresaService.find(empresaId); validateInterval(request.quantidade(), request.inicio(), request.fim());
        sectorScopeService.assertAllowed(empresaId, request.setorId());
        var resources = resolveResources(empresaId, request, empresa);
        ensureNoOverlap(resources.funcionario().getId(), request.inicio(), request.fim(), null);
        var r = new RegistroProducao(); r.setEmpresa(empresa); apply(r, request, resources); r.touchForCreate();
        var saved = repository.save(r);
        // Ponto de auditoria: somente depois da validação e persistência da produção.
        auditoriaService.record(empresaId, TipoRecursoAuditoria.REGISTRO_PRODUCAO, saved.getId(),
                AcaoAuditoria.CRIACAO, OrigemAuditoria.MANUAL, null, productionSnapshot(saved), sectorMetadata(saved), null);
        return mapper.toResponse(saved, 0);
    }

    @Transactional(readOnly = true)
    public Page<RegistroProducaoResponse> list(Long empresaId, Long setorId, Long funcionarioId, Long pecaId,
                                               LocalDateTime inicio, LocalDateTime fim, Boolean ativo, Pageable pageable) {
        empresaService.find(empresaId);
        Specification<RegistroProducao> spec = (root, query, cb) -> cb.equal(root.get("empresa").get("id"), empresaId);
        spec = spec.and((root, query, cb) -> sectorScopeService.restrict(root, cb, empresaId, "setor"));
        if (setorId != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("setor").get("id"), setorId));
        if (funcionarioId != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("funcionario").get("id"), funcionarioId));
        if (pecaId != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("peca").get("id"), pecaId));
        if (inicio != null) spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("inicio"), inicio));
        if (fim != null) spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("fim"), fim));
        if (ativo != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("ativo"), ativo));
        var effective = pageable.getSort().isUnsorted() ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "inicio")) : pageable;
        var page = repository.findAll(spec, effective); var stopped = paradaRepository.findByRegistroProducaoIdInAndAtivoTrue(page.getContent().stream().map(RegistroProducao::getId).toList()).stream().collect(java.util.stream.Collectors.groupingBy(p -> p.getRegistroProducao().getId(), java.util.stream.Collectors.summingLong(p -> java.time.Duration.between(p.getInicio(), p.getFim()).toMinutes())));
        return page.map(r -> mapper.toResponse(r, stopped.getOrDefault(r.getId(), 0L)));
    }

    @Transactional(readOnly = true)
    public RegistroProducao find(Long empresaId, Long id) {
        empresaService.find(empresaId);
        return repository.findOne((root, query, cb) -> cb.and(cb.equal(root.get("id"), id), cb.equal(root.get("empresa").get("id"), empresaId)))
                .orElseThrow(() -> new ResourceNotFoundException("Registro de produção não encontrado nesta empresa."));
    }

    @Transactional(readOnly = true)
    public RegistroProducaoResponse get(Long empresaId, Long id) { var r = find(empresaId, id); return mapper.toResponse(r, paradaRepository.findByRegistroProducaoIdInAndAtivoTrue(java.util.List.of(id)).stream().mapToLong(p -> java.time.Duration.between(p.getInicio(), p.getFim()).toMinutes()).sum()); }

    @Transactional
    public RegistroProducaoResponse update(Long empresaId, Long id, RegistroProducaoRequest request) {
        var current = find(empresaId, id); var before = productionSnapshot(current); validateInterval(request.quantidade(), request.inicio(), request.fim());
        sectorScopeService.assertAllowed(empresaId, request.setorId());
        if (paradaRepository.existsOutsidePeriod(id, request.inicio(), request.fim())) throw new BusinessException("Não é possível alterar o período da produção porque existem paradas fora do novo intervalo.");
        var resources = resolveResources(empresaId, request, empresaService.find(empresaId));
        ensureNoOverlap(resources.funcionario().getId(), request.inicio(), request.fim(), id);
        apply(current, request, resources); current.touchForUpdate(); var saved = repository.save(current);
        auditoriaService.record(empresaId, TipoRecursoAuditoria.REGISTRO_PRODUCAO, id,
                AcaoAuditoria.ALTERACAO, OrigemAuditoria.MANUAL, before, productionSnapshot(saved), sectorMetadata(saved), null);
        return mapper.toResponse(saved, paradaRepository.findByRegistroProducaoIdInAndAtivoTrue(java.util.List.of(id)).stream().mapToLong(p -> java.time.Duration.between(p.getInicio(), p.getFim()).toMinutes()).sum());
    }

    @Transactional
    public RegistroProducaoResponse setActive(Long empresaId, Long id, boolean active) {
        var r = find(empresaId, id); var before = productionSnapshot(r); r.setAtivo(active); r.touchForUpdate(); var saved = repository.save(r);
        auditoriaService.record(empresaId, TipoRecursoAuditoria.REGISTRO_PRODUCAO, id,
                active ? AcaoAuditoria.ATIVACAO : AcaoAuditoria.DESATIVACAO, OrigemAuditoria.MANUAL,
                before, productionSnapshot(saved), sectorMetadata(saved), null);
        return mapper.toResponse(saved);
    }

    private ResourceSet resolveResources(Long empresaId, RegistroProducaoRequest request, Empresa empresa) {
        var setor = setorRepository.findByIdAndEmpresaId(request.setorId(), empresaId).orElseThrow(() -> new ResourceNotFoundException("Setor não encontrado nesta empresa."));
        var funcionario = funcionarioRepository.findByIdAndEmpresaId(request.funcionarioId(), empresaId).orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado nesta empresa."));
        var peca = pecaRepository.findByIdAndEmpresaId(request.pecaId(), empresaId).orElseThrow(() -> new ResourceNotFoundException("Peça não encontrada nesta empresa."));
        if (!setor.isAtivo()) throw new BusinessException("O setor informado está inativo.");
        if (!funcionario.isAtivo()) throw new BusinessException("O funcionário informado está inativo.");
        if (!peca.isAtivo()) throw new BusinessException("A peça informada está inativa.");
        var maquina = request.maquinaId() == null ? null : maquinaRepository.findByIdAndEmpresaId(request.maquinaId(), empresaId).orElseThrow(() -> new ResourceNotFoundException("Máquina não encontrada nesta empresa."));
        if (maquina != null) { if (!maquina.isAtivo()) throw new BusinessException("A máquina informada está inativa."); if (!maquina.getSetor().getId().equals(setor.getId())) throw new BusinessException("A máquina informada pertence a outro setor."); }
        var turno = request.turnoId() == null ? null : turnoRepository.findByIdAndEmpresaId(request.turnoId(), empresaId).orElseThrow(() -> new ResourceNotFoundException("Turno não encontrado nesta empresa."));
        if (turno != null && !turno.isAtivo()) throw new BusinessException("O turno informado está inativo.");
        return new ResourceSet(empresa, setor, funcionario, peca, maquina, turno);
    }

    private void ensureNoOverlap(Long funcionarioId, LocalDateTime inicio, LocalDateTime fim, Long id) {
        if (repository.existsOverlapping(funcionarioId, inicio, fim, id))
            throw new BusinessException("O funcionário já possui um registro de produção sobreposto neste período.");
    }

    private static void validateInterval(BigDecimal quantidade, LocalDateTime inicio, LocalDateTime fim) {
        if (quantidade == null || quantidade.signum() <= 0) throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        if (inicio == null || fim == null || !fim.isAfter(inicio)) throw new IllegalArgumentException("O horário final deve ser posterior ao horário inicial.");
    }

    private static void apply(RegistroProducao r, RegistroProducaoRequest request, ResourceSet resources) {
        r.setEmpresa(resources.empresa()); r.setSetor(resources.setor()); r.setFuncionario(resources.funcionario()); r.setPeca(resources.peca());
        r.setQuantidade(request.quantidade()); r.setInicio(request.inicio()); r.setFim(request.fim()); r.setMaquina(resources.maquina()); r.setTurno(resources.turno()); r.setObservacao(EmpresaService.normalizeText(request.observacao()));
    }

    private static Map<String, Object> productionSnapshot(RegistroProducao r) {
        var values = new LinkedHashMap<String, Object>();
        values.put("id", r.getId()); values.put("setorId", r.getSetor().getId());
        values.put("funcionarioId", r.getFuncionario().getId()); values.put("pecaId", r.getPeca().getId());
        values.put("maquinaId", r.getMaquina() == null ? null : r.getMaquina().getId());
        values.put("turnoId", r.getTurno() == null ? null : r.getTurno().getId());
        values.put("quantidade", r.getQuantidade()); values.put("inicio", r.getInicio());
        values.put("fim", r.getFim()); values.put("observacao", r.getObservacao());
        values.put("ativo", r.isAtivo()); values.put("origem", r.getOrigem());
        values.put("importacaoId", r.getImportacao() == null ? null : r.getImportacao().getId());
        values.put("importacaoLinha", r.getImportacaoLinha());
        return values;
    }

    private static Map<String, Object> sectorMetadata(RegistroProducao r) {
        return Map.of("setorId", r.getSetor().getId());
    }

    private record ResourceSet(Empresa empresa, Setor setor, Funcionario funcionario, Peca peca, Maquina maquina, Turno turno) { }
}
