package com.metalurgica.sistemagestaoindustrial.service;

import com.metalurgica.sistemagestaoindustrial.domain.*;
import com.metalurgica.sistemagestaoindustrial.dto.request.ParadaProducaoRequest;
import com.metalurgica.sistemagestaoindustrial.dto.response.ParadaProducaoResponse;
import com.metalurgica.sistemagestaoindustrial.exception.BusinessException;
import com.metalurgica.sistemagestaoindustrial.exception.ResourceNotFoundException;
import com.metalurgica.sistemagestaoindustrial.mapper.ParadaProducaoMapper;
import com.metalurgica.sistemagestaoindustrial.repository.ParadaProducaoRepository;
import com.metalurgica.sistemagestaoindustrial.repository.MaquinaRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service public class ParadaProducaoService {
    private final ParadaProducaoRepository repository; private final RegistroProducaoService producaoService; private final MotivoParadaService motivoService; private final MaquinaRepository maquinaRepository; private final ParadaProducaoMapper mapper;
    public ParadaProducaoService(ParadaProducaoRepository repository, RegistroProducaoService producaoService, MotivoParadaService motivoService, MaquinaRepository maquinaRepository, ParadaProducaoMapper mapper) { this.repository = repository; this.producaoService = producaoService; this.motivoService = motivoService; this.maquinaRepository = maquinaRepository; this.mapper = mapper; }
    @Transactional public ParadaProducaoResponse create(Long empresaId, Long producaoId, ParadaProducaoRequest req) {
        var producao = validProduction(empresaId, producaoId); var motivo = validReason(empresaId, req.motivoParadaId()); var maquina = validMachine(empresaId, req.maquinaId(), producao); validatePeriod(req.inicio(), req.fim(), producao.getInicio(), producao.getFim()); ensureOverlap(producaoId, req.inicio(), req.fim(), null);
        var p = new ParadaProducao(); p.setEmpresa(producao.getEmpresa()); p.setRegistroProducao(producao); p.setMotivoParada(motivo); p.setMaquina(maquina); apply(p, req); p.touchForCreate(); return mapper.toResponse(repository.save(p));
    }
    @Transactional(readOnly = true) public Page<ParadaProducaoResponse> list(Long empresaId, Long producaoId, Pageable pageable) { findProduction(empresaId, producaoId); return repository.findByEmpresaIdAndRegistroProducaoId(empresaId, producaoId, pageable).map(mapper::toResponse); }
    @Transactional(readOnly = true) public ParadaProducao find(Long empresaId, Long producaoId, Long id) { findProduction(empresaId, producaoId); return repository.findByIdAndEmpresaIdAndRegistroProducaoId(id, empresaId, producaoId).orElseThrow(() -> new ResourceNotFoundException("Parada não encontrada nesta produção.")); }
    @Transactional(readOnly = true) public ParadaProducaoResponse get(Long empresaId, Long producaoId, Long id) { return mapper.toResponse(find(empresaId, producaoId, id)); }
    @Transactional public ParadaProducaoResponse update(Long empresaId, Long producaoId, Long id, ParadaProducaoRequest req) { var p = find(empresaId, producaoId, id); var producao = validProduction(empresaId, producaoId); var motivo = validReason(empresaId, req.motivoParadaId()); validatePeriod(req.inicio(), req.fim(), producao.getInicio(), producao.getFim()); ensureOverlap(producaoId, req.inicio(), req.fim(), id); p.setMotivoParada(motivo); p.setMaquina(validMachine(empresaId, req.maquinaId(), producao)); apply(p, req); p.touchForUpdate(); return mapper.toResponse(repository.save(p)); }
    @Transactional public ParadaProducaoResponse setActive(Long empresaId, Long producaoId, Long id, boolean active) { var p = find(empresaId, producaoId, id); if (active) { var prod = validProduction(empresaId, producaoId); validReason(empresaId, p.getMotivoParada().getId()); validatePeriod(p.getInicio(), p.getFim(), prod.getInicio(), prod.getFim()); ensureOverlap(producaoId, p.getInicio(), p.getFim(), id); } p.setAtivo(active); p.touchForUpdate(); return mapper.toResponse(repository.save(p)); }
    public long activeMinutes(Long producaoId) { return repository.findByRegistroProducaoIdInAndAtivoTrue(List.of(producaoId)).stream().mapToLong(p -> Duration.between(p.getInicio(), p.getFim()).toMinutes()).sum(); }
    public Map<Long, Long> activeMinutesByProduction(Collection<Long> ids) { var map = new HashMap<Long, Long>(); repository.findByRegistroProducaoIdInAndAtivoTrue(ids).forEach(p -> map.merge(p.getRegistroProducao().getId(), Duration.between(p.getInicio(), p.getFim()).toMinutes(), Long::sum)); return map; }
    private RegistroProducao findProduction(Long empresaId, Long id) { return producaoService.find(empresaId, id); }
    private RegistroProducao validProduction(Long empresaId, Long id) { var p = findProduction(empresaId, id); if (!p.isAtivo()) throw new BusinessException("Não é possível operar paradas em uma produção inativa."); return p; }
    private MotivoParada validReason(Long empresaId, Long id) { var m = motivoService.find(empresaId, id); if (!m.isAtivo()) throw new BusinessException("O motivo de parada está inativo."); return m; }
    private void ensureOverlap(Long prod, LocalDateTime inicio, LocalDateTime fim, Long id) { if (repository.existsOverlapping(prod, inicio, fim, id)) throw new BusinessException("Já existe uma parada sobreposta neste período."); }
    private static void validatePeriod(LocalDateTime inicio, LocalDateTime fim, LocalDateTime prodInicio, LocalDateTime prodFim) { if (inicio == null || fim == null || !fim.isAfter(inicio)) throw new IllegalArgumentException("O horário final da parada deve ser posterior ao inicial."); if (inicio.isBefore(prodInicio) || fim.isAfter(prodFim)) throw new BusinessException("A parada deve estar contida no período da produção."); }
    private Maquina validMachine(Long empresaId, Long id, RegistroProducao producao) { if (id == null) return null; var m = maquinaRepository.findByIdAndEmpresaId(id, empresaId).orElseThrow(() -> new ResourceNotFoundException("Máquina não encontrada nesta empresa.")); if (!m.isAtivo()) throw new BusinessException("A máquina informada está inativa."); if (producao.getMaquina() != null && !producao.getMaquina().getId().equals(m.getId())) throw new BusinessException("A máquina da parada é inconsistente com a produção."); return m; }
    private static void apply(ParadaProducao p, ParadaProducaoRequest r) { p.setInicio(r.inicio()); p.setFim(r.fim()); p.setObservacao(r.observacao() == null ? null : EmpresaService.normalizeText(r.observacao())); }
}
