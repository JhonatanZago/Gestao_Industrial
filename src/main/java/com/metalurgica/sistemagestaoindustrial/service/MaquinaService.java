package com.metalurgica.sistemagestaoindustrial.service;
import com.metalurgica.sistemagestaoindustrial.domain.Maquina;
import com.metalurgica.sistemagestaoindustrial.dto.request.MaquinaRequest;
import com.metalurgica.sistemagestaoindustrial.dto.response.MaquinaResponse;
import com.metalurgica.sistemagestaoindustrial.exception.*;
import com.metalurgica.sistemagestaoindustrial.mapper.MaquinaMapper;
import com.metalurgica.sistemagestaoindustrial.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service public class MaquinaService {
    private final MaquinaRepository repository; private final EmpresaService empresaService; private final SetorRepository setorRepository; private final MaquinaMapper mapper;
    public MaquinaService(MaquinaRepository repository, EmpresaService empresaService, SetorRepository setorRepository, MaquinaMapper mapper) { this.repository = repository; this.empresaService = empresaService; this.setorRepository = setorRepository; this.mapper = mapper; }
    @Transactional public MaquinaResponse create(Long empresaId, MaquinaRequest req) { var empresa = empresaService.find(empresaId); var setor = setorRepository.findByIdAndEmpresaId(req.setorId(), empresaId).orElseThrow(() -> new ResourceNotFoundException("Setor não encontrado nesta empresa.")); if (!setor.isAtivo()) throw new BusinessException("O setor informado está inativo."); var codigo = code(req.codigo()); ensureUnique(empresaId, codigo, null); var m = new Maquina(); m.setEmpresa(empresa); m.setSetor(setor); apply(m, req, codigo); m.touchForCreate(); return mapper.toResponse(repository.save(m)); }
    @Transactional(readOnly = true) public Page<MaquinaResponse> list(Long e, Long setor, String codigo, String nome, String tipo, Boolean ativo, Pageable p) { empresaService.find(e); Page<Maquina> page; if (setor != null) page = repository.findByEmpresaIdAndSetorId(e, setor, p); else if (codigo != null && !codigo.isBlank()) page = repository.findByEmpresaIdAndCodigoContainingIgnoreCase(e, codigo.trim(), p); else if (nome != null && !nome.isBlank()) page = repository.findByEmpresaIdAndNomeContainingIgnoreCase(e, nome.trim(), p); else if (tipo != null && !tipo.isBlank()) page = repository.findByEmpresaIdAndTipoContainingIgnoreCase(e, tipo.trim(), p); else if (ativo != null) page = repository.findByEmpresaIdAndAtivo(e, ativo, p); else page = repository.findByEmpresaId(e, p); return page.map(mapper::toResponse); }
    @Transactional(readOnly = true) public Maquina find(Long e, Long id) { empresaService.find(e); return repository.findByIdAndEmpresaId(id, e).orElseThrow(() -> new ResourceNotFoundException("Máquina não encontrada nesta empresa.")); }
    @Transactional(readOnly = true) public MaquinaResponse get(Long e, Long id) { return mapper.toResponse(find(e, id)); }
    @Transactional public MaquinaResponse update(Long e, Long id, MaquinaRequest req) { var m = find(e, id); var setor = setorRepository.findByIdAndEmpresaId(req.setorId(), e).orElseThrow(() -> new ResourceNotFoundException("Setor não encontrado nesta empresa.")); var codigo = code(req.codigo()); ensureUnique(e, codigo, id); m.setSetor(setor); apply(m, req, codigo); m.touchForUpdate(); return mapper.toResponse(repository.save(m)); }
    @Transactional public MaquinaResponse setActive(Long e, Long id, boolean active) { var m = find(e, id); m.setAtivo(active); m.touchForUpdate(); return mapper.toResponse(repository.save(m)); }
    private void ensureUnique(Long e, String c, Long id) { boolean exists = id == null ? repository.existsByEmpresaIdAndCodigo(e, c) : repository.existsByEmpresaIdAndCodigoAndIdNot(e, c, id); if (exists) throw new DuplicateResourceException("Já existe uma máquina com este código nesta empresa."); }
    private static void apply(Maquina m, MaquinaRequest r, String code) { m.setCodigo(code); m.setNome(EmpresaService.normalizeText(r.nome())); m.setDescricao(r.descricao() == null ? null : EmpresaService.normalizeText(r.descricao())); m.setTipo(r.tipo() == null ? null : EmpresaService.normalizeText(r.tipo()).toUpperCase(java.util.Locale.ROOT)); }
    private static String code(String v) { return EmpresaService.normalizeText(v).toUpperCase(java.util.Locale.ROOT); }
}
