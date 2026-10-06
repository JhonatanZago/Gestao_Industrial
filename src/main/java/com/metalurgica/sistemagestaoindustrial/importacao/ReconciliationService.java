package com.metalurgica.sistemagestaoindustrial.importacao;

import com.metalurgica.sistemagestaoindustrial.domain.OrigemImportacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

@Service
public class ReconciliationService {
    private static final Logger LOG = LoggerFactory.getLogger(ReconciliationService.class);
    private final AutomaticImportProperties properties;
    private final FileProcessingCoordinator coordinator;
    private final BootstrapBaselineService baseline;
    private final AtomicBoolean running = new AtomicBoolean();

    @Autowired
    public ReconciliationService(AutomaticImportProperties properties, FileProcessingCoordinator coordinator,
                                 BootstrapBaselineService baseline) {
        this.properties = properties; this.coordinator = coordinator; this.baseline = baseline;
    }

    /** Compatibility constructor for isolated unit tests that do not load repositories. */
    public ReconciliationService(AutomaticImportProperties properties, FileProcessingCoordinator coordinator) {
        this.properties = properties; this.coordinator = coordinator; this.baseline = null;
    }

    @Scheduled(cron = "${industrial.importacao.automatica.reconciliacao.cron:0 0 18 * * *}",
            zone = "${industrial.importacao.automatica.reconciliacao.zone:America/Sao_Paulo}")
    public void scheduledReconciliation() { reconciliar(); }

    public ReconciliationResult reconciliar() {
        var started = OffsetDateTime.now();
        if (!properties.isEnabled() || !properties.getReconciliation().isEnabled()) {
            LOG.info("RECONCILIATION_DISABLED");
            return new ReconciliationResult(started, OffsetDateTime.now(), 0, 0, 0, 0, 0);
        }
        if (!running.compareAndSet(false, true)) {
            LOG.info("RECONCILIATION_ALREADY_RUNNING");
            return new ReconciliationResult(started, OffsetDateTime.now(), 0, 0, 0, 0, 0);
        }
        int found = 0, verified = 0, ignored = 0, processed = 0, errors = 0;
        LOG.info("RECONCILIATION_STARTED");
        try {
            for (var directory : properties.getDirectories()) {
                Path root;
                try {
                    root = Path.of(directory.getPath()).toAbsolutePath().normalize();
                    if (directory.getKind() == AutomaticImportProperties.SourceKind.DIRECTORY && !Files.isDirectory(root)) throw new IllegalStateException("DIRETORIO_INACESSIVEL");
                    if (directory.getKind() == AutomaticImportProperties.SourceKind.FILE && !Files.isRegularFile(root)) throw new IllegalStateException("ARQUIVO_INACESSIVEL");
                    LOG.info("RECONCILIATION_DIRECTORY_SCAN path={}", root.getFileName());
                } catch (Exception error) {
                    errors++;
                    LOG.warn("RECONCILIATION_DIRECTORY_FAILED reason={}", error.getMessage());
                    continue;
                }
                try (Stream<Path> files = directory.getKind() == AutomaticImportProperties.SourceKind.FILE
                        ? Stream.of(root) : Files.list(root)) {
                    for (Path file : files.sorted().toList()) {
                        if (!AutomaticImportFilePolicy.eligible(file, directory)) continue;
                        found++; verified++;
                        var decision = baseline == null ? BootstrapBaselineService.Decision.PROCESS
                                : baseline.evaluate(file, directory, properties.getInitialMode());
                        if (decision != BootstrapBaselineService.Decision.PROCESS) {
                            ignored++;
                            LOG.info("RECONCILIATION_FILE_ALREADY_PROCESSED file={} reason={}", file.getFileName(), decision);
                            continue;
                        }
                        var outcome = coordinator.process(file, directory, properties.getStability(), OrigemImportacao.RECONCILIACAO);
                        switch (outcome) {
                            case PROCESSED -> { if (baseline != null) baseline.markProcessed(file, directory); processed++; LOG.info("RECONCILIATION_FILE_SUCCESS file={}", file.getFileName()); }
                            case ALREADY_PROCESSED -> { if (baseline != null) baseline.markProcessed(file, directory); ignored++; LOG.info("RECONCILIATION_FILE_ALREADY_PROCESSED file={}", file.getFileName()); }
                            case DRY_RUN -> { ignored++; LOG.info("RECONCILIATION_FILE_DRY_RUN file={}", file.getFileName()); }
                            case ERROR -> { errors++; LOG.warn("RECONCILIATION_FILE_FAILED file={}", file.getFileName()); }
                        }
                    }
                } catch (Exception error) {
                    errors++;
                    LOG.warn("RECONCILIATION_DIRECTORY_FAILED reason={}", error.getMessage());
                }
            }
        } finally {
            running.set(false);
        }
        var result = new ReconciliationResult(started, OffsetDateTime.now(), found, verified, ignored, processed, errors);
        LOG.info("RECONCILIATION_COMPLETED directories={} files={} processed={} ignored={} errors={}",
                properties.getDirectories().size(), found, processed, ignored, errors);
        return result;
    }
}
