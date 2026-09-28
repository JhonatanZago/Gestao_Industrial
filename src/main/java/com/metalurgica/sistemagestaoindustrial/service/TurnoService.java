package com.metalurgica.sistemagestaoindustrial.service;
import com.metalurgica.sistemagestaoindustrial.audit.*;
import com.metalurgica.sistemagestaoindustrial.domain.Turno;
import com.metalurgica.sistemagestaoindustrial.dto.request.TurnoRequest;
import com.metalurgica.sistemagestaoindustrial.dto.response.TurnoResponse;
import com.metalurgica.sistemagestaoindustrial.exception.*;
import com.metalurgica.sistemagestaoindustrial.mapper.TurnoMapper;
import com.metalurgica.sistemagestaoindustrial.repository.TurnoRepository;
import org.springframework.data.domain.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.util.LinkedHashMap; import java.util.Map;
@Service public class TurnoService {
    private final TurnoRepository repository; private final EmpresaService empresaService; private final TurnoMapper mapper; private final AuditoriaService auditoriaService;
    public TurnoService(TurnoRepository repository, EmpresaService empresaService, TurnoMapper mapper, AuditoriaService auditoriaService) { this.repository = repository; this.empresaService = empresaService; this.mapper = mapper; this.auditoriaService = auditoriaService; }
    @Transactional public TurnoResponse create(Long e, TurnoRequest r) { var t = new Turno(); t.setEmpresa(empresaService.find(e)); validate(r); var c = code(r.codigo()); unique(e, c, null); apply(t, r, c); t.touchForCreate(); var saved = repository.save(t); auditoriaService.record(e, TipoRecursoAuditoria.TURNO, saved.getId(), AcaoAuditoria.CRIACAO, OrigemAuditoria.MANUAL, null, snapshot(saved), null, null); return mapper.toResponse(saved); }
    @Transactional(readOnly = true) public Page<TurnoResponse> list(Long e, Boolean ativo, String codigo, String nome, Pageable p) { empresaService.find(e); Page<Turno> page; if (codigo != null && !codigo.isBlank()) page = repository.findByEmpresaIdAndCodigoContainingIgnoreCase(e, codigo.trim(), p); else if (nome != null && !nome.isBlank()) page = repository.findByEmpresaIdAndNomeContainingIgnoreCase(e, nome.trim(), p); else if (ativo != null) page = repository.findByEmpresaIdAndAtivo(e, ativo, p); else page = repository.findByEmpresaId(e, p); return page.map(mapper::toResponse); }
    @Transactional(readOnly = true) public Turno find(Long e, Long id) { empresaService.find(e); return repository.findByIdAndEmpresaId(id, e).orElseThrow(() -> new ResourceNotFoundException("Turno não encontrado nesta empresa.")); }
    @Transactional(readOnly = true) public TurnoResponse get(Long e, Long id) { return mapper.toResponse(find(e, id)); }
    @Transactional public TurnoResponse update(Long e, Long id, TurnoRequest r) { var t = find(e, id); var before = snapshot(t); validate(r); var c = code(r.codigo()); unique(e, c, id); apply(t, r, c); t.touchForUpdate(); var saved = repository.save(t); auditoriaService.record(e, TipoRecursoAuditoria.TURNO, id, AcaoAuditoria.ALTERACAO, OrigemAuditoria.MANUAL, before, snapshot(saved), null, null); return mapper.toResponse(saved); }
    @Transactional public TurnoResponse setActive(Long e, Long id, boolean active) { var t = find(e, id); var before = snapshot(t); t.setAtivo(active); t.touchForUpdate(); var saved = repository.save(t); auditoriaService.record(e, TipoRecursoAuditoria.TURNO, id, active ? AcaoAuditoria.ATIVACAO : AcaoAuditoria.DESATIVACAO, OrigemAuditoria.MANUAL, before, snapshot(saved), null, null); return mapper.toResponse(saved); }
    private void unique(Long e, String c, Long id) { if (id == null ? repository.existsByEmpresaIdAndCodigo(e, c) : repository.existsByEmpresaIdAndCodigoAndIdNot(e, c, id)) throw new DuplicateResourceException("Já existe um turno com este código nesta empresa."); }
    private static void validate(TurnoRequest r) { if (r.horaInicio().equals(r.horaFim())) throw new IllegalArgumentException("O início e o fim do turno não podem ser iguais."); }
    private static void apply(Turno t, TurnoRequest r, String c) { t.setCodigo(c); t.setNome(EmpresaService.normalizeText(r.nome())); t.setHoraInicio(r.horaInicio()); t.setHoraFim(r.horaFim()); }
    private static String code(String v) { return EmpresaService.normalizeText(v).toUpperCase(java.util.Locale.ROOT); }
    private static Map<String, Object> snapshot(Turno t) { var values = new LinkedHashMap<String, Object>(); values.put("id", t.getId()); values.put("codigo", t.getCodigo()); values.put("nome", t.getNome()); values.put("horaInicio", t.getHoraInicio()); values.put("horaFim", t.getHoraFim()); values.put("ativo", t.isAtivo()); return values; }
}
