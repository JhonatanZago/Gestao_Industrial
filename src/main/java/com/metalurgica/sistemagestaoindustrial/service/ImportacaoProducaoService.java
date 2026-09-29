package com.metalurgica.sistemagestaoindustrial.service;

import com.metalurgica.sistemagestaoindustrial.domain.ImportacaoProducao;
import com.metalurgica.sistemagestaoindustrial.domain.StatusImportacao;
import com.metalurgica.sistemagestaoindustrial.dto.response.ImportacaoProducaoResponse;
import com.metalurgica.sistemagestaoindustrial.dto.response.ResultadoLinhaImportacaoResponse;
import com.metalurgica.sistemagestaoindustrial.exception.*;
import com.metalurgica.sistemagestaoindustrial.importacao.*;
import com.metalurgica.sistemagestaoindustrial.mapper.ImportacaoProducaoMapper;
import com.metalurgica.sistemagestaoindustrial.observability.*;
import com.metalurgica.sistemagestaoindustrial.repository.*;
import com.metalurgica.sistemagestaoindustrial.security.SectorScopeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
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
    private final ImportacaoProducaoMapper mapper;
    private final int maxLinhas;
    private final SectorScopeService sectorScopeService;
    private final ImportacaoLifecycleService lifecycle;
    private final OperationalMetrics metrics;

    public ImportacaoProducaoService(ImportacaoProducaoRepository repository,
            ResultadoLinhaImportacaoRepository resultadoRepository, EmpresaService empresaService,
            SetorRepository setorRepository, ExcelImportReader reader, ImportacaoValidator validator,
            ImportacaoProducaoMapper mapper, @Value("${app.importacao.max-linhas:10000}") int maxLinhas,
            SectorScopeService sectorScopeService, ImportacaoLifecycleService lifecycle,
            OperationalMetrics metrics) {
        this.repository = repository;
        this.resultadoRepository = resultadoRepository;
        this.empresaService = empresaService;
        this.setorRepository = setorRepository;
        this.reader = reader;
        this.validator = validator;
        this.mapper = mapper;
        this.maxLinhas = maxLinhas;
        this.sectorScopeService = sectorScopeService;
        this.lifecycle = lifecycle;
        this.metrics = metrics;
    }

    public ImportacaoProducaoResponse validate(Long empresaId, Long setorId, MultipartFile file) {
        return process(empresaId, setorId, file, false);
    }

    public ImportacaoProducaoResponse importFile(Long empresaId, Long setorId, MultipartFile file) {
        return process(empresaId, setorId, file, true);
    }

    @Transactional(readOnly = true)
    public Page<ImportacaoProducaoResponse> list(Long empresaId, Long setorId, StatusImportacao status,
            String nomeArquivo, OffsetDateTime dataInicio, OffsetDateTime dataFim, Pageable pageable) {
        empresaService.find(empresaId);
        sectorScopeService.assertAllowed(empresaId, setorId);
        Specification<ImportacaoProducao> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("empresa").get("id"), empresaId),
                sectorScopeService.restrict(root, cb, empresaId, "setor"));
        if (setorId != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("setor").get("id"), setorId));
        if (status != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        if (nomeArquivo != null && !nomeArquivo.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("nomeArquivo")),
                    "%" + nomeArquivo.toLowerCase(Locale.ROOT) + "%"));
        }
        if (dataInicio != null) spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), dataInicio));
        if (dataFim != null) spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), dataFim));
        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ImportacaoProducaoResponse> list(Long empresaId, Long setorId, StatusImportacao status, Pageable pageable) {
        return list(empresaId, setorId, status, null, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public ImportacaoProducaoResponse get(Long empresaId, Long id) { return mapper.toResponse(find(empresaId, id)); }

    @Transactional(readOnly = true)
    public Page<ResultadoLinhaImportacaoResponse> lines(Long empresaId, Long id, Pageable pageable) {
        find(empresaId, id);
        return resultadoRepository.findByImportacaoId(id, pageable).map(mapper::toResponse);
    }

    private ImportacaoProducaoResponse process(Long empresaId, Long setorId, MultipartFile file, boolean persist) {
        sectorScopeService.assertAllowed(empresaId, setorId);
        empresaService.find(empresaId);
        setorRepository.findByIdAndEmpresaId(setorId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Setor não encontrado nesta empresa."));
        String hash = hash(file);
        if (repository.existsByEmpresaIdAndSetorIdAndHashArquivoAndStatus(
                empresaId, setorId, hash, StatusImportacao.CONCLUIDA)) {
            throw new DuplicateResourceException("ARQUIVO_DUPLICADO");
        }

        long started = metrics.start();
        metrics.importStarted();
        Long importacaoId = null;
        String correlationId = CorrelationContext.current();
        try {
            String filename = file == null ? "arquivo" : Objects.requireNonNullElse(file.getOriginalFilename(), "arquivo.xlsx");
            importacaoId = lifecycle.create(empresaId, setorId, sanitizeFileName(filename), hash, correlationId);
            LOG.info("import_started importacaoId={} empresaId={} setorId={}", importacaoId, empresaId, setorId);

            ExcelReadResult read;
            try {
                read = reader.read(file);
                if (read.totalLinhas() > maxLinhas) throw new IllegalArgumentException("LIMITE_LINHAS_EXCEDIDO");
            } catch (RuntimeException ex) {
                var error = new ImportacaoLinhaErro(0, "arquivo", "ARQUIVO_INVALIDO",
                        "Arquivo rejeitado.", null, null);
                var rejected = lifecycle.reject(importacaoId,
                        new ValidacaoImportacaoResult(List.of(), List.of(error), 0),
                        "ARQUIVO_INVALIDO", "Arquivo rejeitado.");
                metrics.importFinished(OperationalMetrics.Outcome.REJECTED, started, 0);
                LOG.warn("import_rejected importacaoId={} reason={}", importacaoId, LogSanitizer.safe(ex.getMessage()));
                return rejected;
            }

            var validation = validator.validate(empresaId, setorId, read);
            if (!validation.isValida()) {
                var rejected = lifecycle.reject(importacaoId, validation,
                        "VALIDATION_ERROR", "Arquivo rejeitado por validação.");
                metrics.importFinished(OperationalMetrics.Outcome.REJECTED, started, validation.totalLinhas());
                LOG.info("import_rejected importacaoId={} valid={} invalid={}", importacaoId,
                        validation.validas().size(), validation.erros().size());
                return rejected;
            }
            if (!persist) {
                var validated = lifecycle.validated(importacaoId, validation);
                metrics.importFinished(OperationalMetrics.Outcome.SUCCESS, started, validation.totalLinhas());
                LOG.info("import_validated importacaoId={} lines={}", importacaoId, validation.totalLinhas());
                return validated;
            }

            var completed = lifecycle.complete(importacaoId, validation);
            metrics.importFinished(OperationalMetrics.Outcome.SUCCESS, started, validation.totalLinhas());
            LOG.info("import_completed importacaoId={} imported={}", importacaoId, validation.validas().size());
            return completed;
        } catch (RuntimeException ex) {
            if (importacaoId != null) {
                try {
                    lifecycle.fail(importacaoId, "IMPORT_PERSISTENCE_FAILURE", "Falha técnica ao processar importação.");
                } catch (RuntimeException persistenceFailure) {
                    ex.addSuppressed(persistenceFailure);
                }
            }
            metrics.importFinished(OperationalMetrics.Outcome.FAILED, started, 0);
            LOG.error("import_failed importacaoId={}", importacaoId, ex);
            if (ex instanceof ImportProcessingException importError) throw importError;
            throw new ImportProcessingException(ex);
        }
    }

    private ImportacaoProducao find(Long empresaId, Long id) {
        empresaService.find(empresaId);
        return repository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Importação não encontrada nesta empresa."));
    }

    private static String sanitizeFileName(String name) {
        var clean = name.replace('\\', '/');
        clean = clean.substring(clean.lastIndexOf('/') + 1).replaceAll("[^A-Za-z0-9._-]", "_");
        return clean.isBlank() ? "arquivo" : clean.substring(0, Math.min(255, clean.length()));
    }

    private static String hash(MultipartFile file) {
        try {
            byte[] content = file == null ? new byte[0] : file.getBytes();
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (Exception ex) {
            throw new IllegalStateException("Não foi possível calcular hash do arquivo.", ex);
        }
    }
}
