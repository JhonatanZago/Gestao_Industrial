package com.metalurgica.sistemagestaoindustrial.importacao;

import com.metalurgica.sistemagestaoindustrial.domain.OrigemImportacao;
import com.metalurgica.sistemagestaoindustrial.exception.DuplicateResourceException;
import com.metalurgica.sistemagestaoindustrial.service.ImportacaoProducaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class FileProcessingCoordinator {
    private static final Logger LOG = LoggerFactory.getLogger(FileProcessingCoordinator.class);
    private final FileStabilityService stability;
    private final ImportacaoProducaoService importacao;
    private final AutomaticImportProperties properties;
    private final Map<Path, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Autowired
    public FileProcessingCoordinator(FileStabilityService stability, ImportacaoProducaoService importacao,
                                     AutomaticImportProperties properties) {
        this.stability = stability; this.importacao = importacao; this.properties = properties;
    }

    public FileProcessingCoordinator(FileStabilityService stability, ImportacaoProducaoService importacao) {
        this(stability, importacao, new AutomaticImportProperties());
    }

    public Outcome process(Path file, AutomaticImportProperties.Directory directory,
                           AutomaticImportProperties.Stability stabilityConfig, OrigemImportacao origem) {
        var normalized = file.toAbsolutePath().normalize();
        var lock = locks.computeIfAbsent(normalized, ignored -> new ReentrantLock());
        lock.lock();
        try {
            stability.awaitStable(normalized, stabilityConfig);
            if (dryRun()) {
                var hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(normalized)));
                LOG.info("AUTO_IMPORT_DRY_RUN file={} hash={} origem={}", normalized.getFileName(), hash, origem);
                return Outcome.DRY_RUN;
            }
            var multipart = new PathMultipartFile(normalized);
            var result = origem == OrigemImportacao.RECONCILIACAO
                    ? importacao.importFileFromReconciliation(directory.getEmpresaId(), directory.getSetorId(), multipart)
                    : importacao.importFileFromWatcher(directory.getEmpresaId(), directory.getSetorId(), multipart);
            return "CONCLUIDA".equals(result.status()) ? Outcome.PROCESSED : Outcome.ERROR;
        } catch (DuplicateResourceException duplicate) {
            return Outcome.ALREADY_PROCESSED;
        } catch (Exception error) {
            LOG.warn("FILE_PROCESSING_FAILED file={} origem={} reason={}", normalized.getFileName(), origem, error.getMessage());
            return Outcome.ERROR;
        } finally {
            lock.unlock();
        }
    }

    private boolean dryRun() { return properties.isDryRun(); }
    public enum Outcome { PROCESSED, ALREADY_PROCESSED, DRY_RUN, ERROR }
}
