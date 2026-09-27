package com.metalurgica.sistemagestaoindustrial.service;
import com.metalurgica.sistemagestaoindustrial.domain.Turno;
import com.metalurgica.sistemagestaoindustrial.dto.request.TurnoRequest;
import com.metalurgica.sistemagestaoindustrial.dto.response.TurnoResponse;
import com.metalurgica.sistemagestaoindustrial.exception.*;
import com.metalurgica.sistemagestaoindustrial.mapper.TurnoMapper;
import com.metalurgica.sistemagestaoindustrial.repository.TurnoRepository;
import org.springframework.data.domain.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service public class TurnoService {
    private final TurnoRepository repository; private final EmpresaService empresaService; private final TurnoMapper mapper;
    public TurnoService(TurnoRepository repository, EmpresaService empresaService, TurnoMapper mapper) { this.repository = repository; this.empresaService = empresaService; this.mapper = mapper; }
    @Transactional public TurnoResponse create(Long e, TurnoRequest r) { var t = new Turno(); t.setEmpresa(empresaService.find(e)); validate(r); var c = code(r.codigo()); unique(e, c, null); apply(t, r, c); t.touchForCreate(); return mapper.toResponse(repository.save(t)); }
    @Transactional(readOnly = true) public Page<TurnoResponse> list(Long e, Boolean ativo, String codigo, String nome, Pageable p) { empresaService.find(e); Page<Turno> page; if (codigo != null && !codigo.isBlank()) page = repository.findByEmpresaIdAndCodigoContainingIgnoreCase(e, codigo.trim(), p); else if (nome != null && !nome.isBlank()) page = repository.findByEmpresaIdAndNomeContainingIgnoreCase(e, nome.trim(), p); else if (ativo != null) page = repository.findByEmpresaIdAndAtivo(e, ativo, p); else page = repository.findByEmpresaId(e, p); return page.map(mapper::toResponse); }
    @Transactional(readOnly = true) public Turno find(Long e, Long id) { empresaService.find(e); return repository.findByIdAndEmpresaId(id, e).orElseThrow(() -> new ResourceNotFoundException("Turno não encontrado nesta empresa.")); }
    @Transactional(readOnly = true) public TurnoResponse get(Long e, Long id) { return mapper.toResponse(find(e, id)); }
    @Transactional public TurnoResponse update(Long e, Long id, TurnoRequest r) { var t = find(e, id); validate(r); var c = code(r.codigo()); unique(e, c, id); apply(t, r, c); t.touchForUpdate(); return mapper.toResponse(repository.save(t)); }
    @Transactional public TurnoResponse setActive(Long e, Long id, boolean active) { var t = find(e, id); t.setAtivo(active); t.touchForUpdate(); return mapper.toResponse(repository.save(t)); }
    private void unique(Long e, String c, Long id) { if (id == null ? repository.existsByEmpresaIdAndCodigo(e, c) : repository.existsByEmpresaIdAndCodigoAndIdNot(e, c, id)) throw new DuplicateResourceException("Já existe um turno com este código nesta empresa."); }
    private static void validate(TurnoRequest r) { if (r.horaInicio().equals(r.horaFim())) throw new IllegalArgumentException("O início e o fim do turno não podem ser iguais."); }
    private static void apply(Turno t, TurnoRequest r, String c) { t.setCodigo(c); t.setNome(EmpresaService.normalizeText(r.nome())); t.setHoraInicio(r.horaInicio()); t.setHoraFim(r.horaFim()); }
    private static String code(String v) { return EmpresaService.normalizeText(v).toUpperCase(java.util.Locale.ROOT); }
}
