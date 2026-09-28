package com.metalurgica.sistemagestaoindustrial.service;

import com.metalurgica.sistemagestaoindustrial.domain.*;
import com.metalurgica.sistemagestaoindustrial.audit.*;
import com.metalurgica.sistemagestaoindustrial.dto.response.*;
import com.metalurgica.sistemagestaoindustrial.exception.*;
import com.metalurgica.sistemagestaoindustrial.importacao.*;
import com.metalurgica.sistemagestaoindustrial.mapper.ImportacaoProducaoMapper;
import com.metalurgica.sistemagestaoindustrial.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*; import org.springframework.data.jpa.domain.Specification;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.metalurgica.sistemagestaoindustrial.security.SectorScopeService;
import java.security.*;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class ImportacaoProducaoService {
    private static final Logger LOG = LoggerFactory.getLogger(ImportacaoProducaoService.class);
    private final ImportacaoProducaoRepository repository;
    private final ResultadoLinhaImportacaoRepository resultadoRepository;
    private final EmpresaService empresaService;
    private final SetorRepository setorRepository;
    private final ExcelImportReader reader;
    private final ImportacaoValidator validator;
    private final RegistroProducaoRepository producaoRepository;
    private final ImportacaoProducaoMapper mapper;
    private final int maxLinhas;
    private final SectorScopeService sectorScopeService;
    private final AuditoriaService auditoriaService;

    public ImportacaoProducaoService(ImportacaoProducaoRepository repository, ResultadoLinhaImportacaoRepository resultadoRepository, EmpresaService empresaService, SetorRepository setorRepository, ExcelImportReader reader, ImportacaoValidator validator, RegistroProducaoRepository producaoRepository, ImportacaoProducaoMapper mapper, @Value("${app.importacao.max-linhas:10000}") int maxLinhas, SectorScopeService sectorScopeService, AuditoriaService auditoriaService) {
        this.repository = repository; this.resultadoRepository = resultadoRepository; this.empresaService = empresaService; this.setorRepository = setorRepository; this.reader = reader; this.validator = validator; this.producaoRepository = producaoRepository; this.mapper = mapper; this.maxLinhas = maxLinhas; this.sectorScopeService = sectorScopeService; this.auditoriaService = auditoriaService;
    }

    @Transactional public ImportacaoProducaoResponse validate(Long empresaId, Long setorId, MultipartFile file) { return process(empresaId, setorId, file, false); }
    @Transactional public ImportacaoProducaoResponse importFile(Long empresaId, Long setorId, MultipartFile file) { return process(empresaId, setorId, file, true); }
    @Transactional(readOnly = true) public Page<ImportacaoProducaoResponse> list(Long empresaId, Long setorId, StatusImportacao status, String nomeArquivo, OffsetDateTime dataInicio, OffsetDateTime dataFim, Pageable pageable) { empresaService.find(empresaId); sectorScopeService.assertAllowed(empresaId, setorId); Specification<ImportacaoProducao> spec = (root, query, cb) -> cb.and(cb.equal(root.get("empresa").get("id"), empresaId), sectorScopeService.restrict(root, cb, empresaId, "setor")); if (setorId != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("setor").get("id"), setorId)); if (status != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status)); if (nomeArquivo != null && !nomeArquivo.isBlank()) spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("nomeArquivo")), "%" + nomeArquivo.toLowerCase(Locale.ROOT) + "%")); if (dataInicio != null) spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), dataInicio)); if (dataFim != null) spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), dataFim)); return repository.findAll(spec, pageable).map(mapper::toResponse); }
    @Transactional(readOnly = true) public Page<ImportacaoProducaoResponse> list(Long empresaId, Long setorId, StatusImportacao status, Pageable pageable) { return list(empresaId, setorId, status, null, null, null, pageable); }
    @Transactional(readOnly = true) public ImportacaoProducaoResponse get(Long empresaId, Long id) { return mapper.toResponse(find(empresaId, id)); }
    @Transactional(readOnly = true) public Page<ResultadoLinhaImportacaoResponse> lines(Long empresaId, Long id, Pageable pageable) { find(empresaId, id); return resultadoRepository.findByImportacaoId(id, pageable).map(mapper::toResponse); }

    private ImportacaoProducaoResponse process(Long empresaId, Long setorId, MultipartFile file, boolean persist) {
        sectorScopeService.assertAllowed(empresaId, setorId);
        var empresa = empresaService.find(empresaId); var setor = setorRepository.findByIdAndEmpresaId(setorId, empresaId).orElseThrow(() -> new ResourceNotFoundException("Setor não encontrado nesta empresa."));
        String hash = hash(file); if (repository.existsByEmpresaIdAndSetorIdAndHashArquivoAndStatus(empresaId, setorId, hash, StatusImportacao.CONCLUIDA)) throw new DuplicateResourceException("ARQUIVO_DUPLICADO");
        var importacao = new ImportacaoProducao(); importacao.setEmpresa(empresa); importacao.setSetor(setor); importacao.setNomeArquivo(sanitizeFileName(file == null ? "arquivo" : Objects.requireNonNullElse(file.getOriginalFilename(), "arquivo.xlsx"))); importacao.setHashArquivo(hash); importacao.touchCreate(); importacao.setStatus(StatusImportacao.VALIDANDO); repository.save(importacao); LOG.info("importacao iniciada importacaoId={} empresaId={} setorId={} arquivo={}", importacao.getId(), empresaId, setorId, importacao.getNomeArquivo());
        ExcelReadResult read;
        try { read = reader.read(file); if (read.totalLinhas() > maxLinhas) throw new IllegalArgumentException("LIMITE_LINHAS_EXCEDIDO"); }
        catch (RuntimeException ex) { importacao.setStatus(StatusImportacao.REJEITADA); importacao.setFinalizadoEm(OffsetDateTime.now()); importacao.setTotalLinhas(0); importacao.setLinhasInvalidas(1); saveErrors(importacao, List.of(new ImportacaoLinhaErro(0, "arquivo", ex.getMessage(), "Arquivo rejeitado.", null, null))); repository.save(importacao); auditImport(empresaId, importacao); LOG.warn("importacao rejeitada importacaoId={} empresaId={} setorId={} codigo={}", importacao.getId(), empresaId, setorId, ex.getMessage()); return mapper.toResponse(importacao); }
        var validation = validator.validate(empresaId, setorId, read); importacao.setTotalLinhas(validation.totalLinhas()); importacao.setLinhasValidas(validation.validas().size()); importacao.setLinhasInvalidas(validation.erros().size()); saveErrors(importacao, validation.erros());
        if (!validation.isValida()) { importacao.setStatus(StatusImportacao.REJEITADA); importacao.setFinalizadoEm(OffsetDateTime.now()); LOG.info("importacao rejeitada importacaoId={} empresaId={} setorId={} validas={} invalidas={}", importacao.getId(), empresaId, setorId, importacao.getLinhasValidas(), importacao.getLinhasInvalidas()); var saved = repository.save(importacao); auditImport(empresaId, saved); return mapper.toResponse(saved); }
        if (!persist) { importacao.setStatus(StatusImportacao.VALIDADA); importacao.setFinalizadoEm(OffsetDateTime.now()); var saved = repository.save(importacao); auditImport(empresaId, saved); return mapper.toResponse(saved); }
        importacao.setStatus(StatusImportacao.IMPORTANDO); repository.save(importacao);
        for (var line : validation.validas()) { var r = new RegistroProducao(); r.setEmpresa(empresa); r.setSetor(setor); r.setFuncionario(line.funcionario()); r.setPeca(line.peca()); r.setMaquina(line.maquina()); r.setTurno(line.turno()); r.setQuantidade(line.quantidade()); r.setInicio(line.inicio()); r.setFim(line.fim()); r.setOrigem(OrigemProducao.IMPORTACAO); r.setImportacao(importacao); r.setImportacaoLinha(line.linha().linha()); r.touchForCreate(); var saved = producaoRepository.save(r); auditoriaService.record(empresaId, TipoRecursoAuditoria.REGISTRO_PRODUCAO, saved.getId(), AcaoAuditoria.CRIACAO, OrigemAuditoria.IMPORTACAO_EXCEL, null, productionSnapshot(saved), Map.of("setorId", setorId), importacao.getId().toString()); }
        importacao.setLinhasImportadas(validation.validas().size()); importacao.setStatus(StatusImportacao.CONCLUIDA); importacao.setFinalizadoEm(OffsetDateTime.now()); LOG.info("importacao concluida importacaoId={} empresaId={} setorId={} importadas={}", importacao.getId(), empresaId, setorId, importacao.getLinhasImportadas()); var saved = repository.save(importacao); auditImport(empresaId, saved); return mapper.toResponse(saved);
    }
    private void saveErrors(ImportacaoProducao importacao, List<ImportacaoLinhaErro> errors) { resultadoRepository.saveAll(errors.stream().map(e -> { var r = new ResultadoLinhaImportacao(); r.setImportacao(importacao); r.setNumeroLinha(e.linha()); r.setStatus("ERRO"); r.setCodigoFuncionario(e.codigoFuncionario()); r.setCodigoPeca(e.codigoPeca()); r.setCodigoErro(e.codigo()); r.setMensagem(e.mensagem()); return r; }).toList()); }
    private void auditImport(Long empresaId, ImportacaoProducao i) { var metadata = new LinkedHashMap<String, Object>(); metadata.put("importacaoId", i.getId()); metadata.put("arquivoNome", i.getNomeArquivo()); metadata.put("setorId", i.getSetor().getId()); metadata.put("totalLinhas", i.getTotalLinhas()); metadata.put("linhasValidas", i.getLinhasValidas()); metadata.put("linhasInvalidas", i.getLinhasInvalidas()); metadata.put("linhasImportadas", i.getLinhasImportadas()); metadata.put("status", i.getStatus()); metadata.put("dataHora", i.getFinalizadoEm()); auditoriaService.record(empresaId, TipoRecursoAuditoria.IMPORTACAO, i.getId(), AcaoAuditoria.IMPORTACAO, OrigemAuditoria.IMPORTACAO_EXCEL, null, metadata, metadata, i.getId().toString()); }
    private static String sanitizeFileName(String name) { var clean = name.replace('\\', '/'); clean = clean.substring(clean.lastIndexOf('/') + 1).replaceAll("[^A-Za-z0-9._-]", "_"); return clean.isBlank() ? "arquivo" : clean.substring(0, Math.min(255, clean.length())); }
    private static Map<String, Object> productionSnapshot(RegistroProducao r) { var values = new LinkedHashMap<String, Object>(); values.put("id", r.getId()); values.put("setorId", r.getSetor().getId()); values.put("funcionarioId", r.getFuncionario().getId()); values.put("pecaId", r.getPeca().getId()); values.put("maquinaId", r.getMaquina() == null ? null : r.getMaquina().getId()); values.put("turnoId", r.getTurno() == null ? null : r.getTurno().getId()); values.put("quantidade", r.getQuantidade()); values.put("inicio", r.getInicio()); values.put("fim", r.getFim()); values.put("ativo", r.isAtivo()); values.put("origem", r.getOrigem()); values.put("importacaoId", r.getImportacao() == null ? null : r.getImportacao().getId()); values.put("importacaoLinha", r.getImportacaoLinha()); return values; }
    private ImportacaoProducao find(Long empresaId, Long id) { empresaService.find(empresaId); return repository.findByIdAndEmpresaId(id, empresaId).orElseThrow(() -> new ResourceNotFoundException("Importação não encontrada nesta empresa.")); }
    private static String hash(MultipartFile file) { try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(file == null ? new byte[0] : file.getBytes())); } catch (Exception e) { throw new IllegalStateException("Não foi possível calcular hash do arquivo."); } }
}
