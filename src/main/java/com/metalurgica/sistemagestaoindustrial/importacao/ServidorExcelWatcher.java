package com.metalurgica.sistemagestaoindustrial.importacao;

import com.metalurgica.sistemagestaoindustrial.service.ImportacaoProducaoService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class ServidorExcelWatcher {
    private static final Logger LOG = LoggerFactory.getLogger(ServidorExcelWatcher.class);
    private final AutomaticImportProperties properties;
    private final FileProcessingCoordinator coordinator;
    private final ExecutorService executor = Executors.newFixedThreadPool(2, runnable -> {
        var thread = new Thread(runnable, "excel-file-processing"); thread.setDaemon(true); return thread;
    });
    private final Map<Path, AutomaticImportProperties.Directory> contexts = new HashMap<>();
    private FileChangeDebouncer debouncer;
    private FileChangeDetector detector;
    private volatile boolean running;

    @Autowired
    public ServidorExcelWatcher(AutomaticImportProperties properties, FileProcessingCoordinator coordinator) {
        this.properties = properties; this.coordinator = coordinator;
    }

    public ServidorExcelWatcher(AutomaticImportProperties properties, FileStabilityService stability,
                                ImportacaoProducaoService importacao) {
        this(properties, new FileProcessingCoordinator(stability, importacao));
    }

    @PostConstruct
    public void start() {
        if (!properties.isEnabled()) { LOG.info("AUTO_IMPORT_WATCHER_DISABLED"); return; }
        try {
            var paths = new ArrayList<Path>();
            for (var directory : properties.getDirectories()) {
                validate(directory);
                var path = AutomaticImportFilePolicy.watchRoot(directory);
                contexts.put(path, directory); paths.add(path);
            }
            detector = new WatchServiceFileChangeDetector(paths);
            detector.start(this::onChange);
            running = true;
            debouncer = new FileChangeDebouncer(properties.getDebounceMs(), file -> {
                var context = contexts.entrySet().stream().filter(entry -> file.getParent().equals(entry.getKey()))
                        .map(Map.Entry::getValue).findFirst().orElse(null);
                if (context != null) executor.submit(() -> process(file, context));
            });
            LOG.info("AUTO_IMPORT_WATCHER_STARTED directories={}", paths.size());
        } catch (Exception error) {
            LOG.error("AUTO_IMPORT_WATCHER_START_FAILED", error);
            stop();
        }
    }

    void onChange(FileChangeDetector.FileChangeEvent event) {
        var file = event.path().toAbsolutePath().normalize();
        if (event.kind() == FileChangeDetector.FileChangeEvent.Kind.DELETE) {
            LOG.info("FILE_REMOVED file={}", file.getFileName());
            return;
        }
        var directory = contexts.entrySet().stream().filter(entry -> file.getParent().equals(entry.getKey()))
                .map(Map.Entry::getValue).findFirst().orElse(null);
        if (directory == null) return;
        if (!AutomaticImportFilePolicy.eligible(file, directory)) return;
        LOG.info("FILE_CHANGE_DETECTED file={}", file.getFileName());
        debouncer.signal(file);
    }

    private void process(Path file, AutomaticImportProperties.Directory directory) {
        var outcome = coordinator.process(file, directory, properties.getStability(), com.metalurgica.sistemagestaoindustrial.domain.OrigemImportacao.FILE_WATCHER);
        switch (outcome) {
            case PROCESSED -> LOG.info("AUTO_IMPORT_SUCCESS file={}", file.getFileName());
            case ALREADY_PROCESSED -> LOG.info("FILE_VERSION_ALREADY_PROCESSED file={}", file.getFileName());
            case ERROR -> LOG.error("AUTO_IMPORT_FAILED file={}", file.getFileName());
        }
    }

    public static boolean relevant(Path file) {
        var name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return Files.isRegularFile(file) && name.endsWith(".xlsx") && !name.startsWith("~$")
                && !name.endsWith(".tmp.xlsx") && !name.endsWith(".lock.xlsx") && !name.startsWith(".");
    }

    private static void validate(AutomaticImportProperties.Directory directory) {
        if (directory.getEmpresaId() == null || directory.getSetorId() == null || directory.getPath() == null || directory.getPath().isBlank())
            throw new IllegalArgumentException("Configuração de diretório automático incompleta");
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (debouncer != null) debouncer.close();
        if (detector != null) try { detector.stop(); } catch (IOException error) { LOG.warn("AUTO_IMPORT_WATCHER_STOP_FAILED", error); }
        executor.shutdownNow();
        LOG.info("AUTO_IMPORT_WATCHER_STOPPED");
    }

    public boolean isRunning() { return running; }
}
