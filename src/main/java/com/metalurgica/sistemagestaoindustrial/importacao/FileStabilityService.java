package com.metalurgica.sistemagestaoindustrial.importacao;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@Service
public class FileStabilityService {
    public void awaitStable(Path path, AutomaticImportProperties.Stability config) throws IOException {
        if (config.getIntervalMs() < 1 || config.getStableChecks() < 1 || config.getTimeoutSeconds() < 1)
            throw new IllegalArgumentException("Configuração de estabilidade inválida");
        long deadline = System.nanoTime() + Duration.ofSeconds(config.getTimeoutSeconds()).toNanos();
        long previousSize = -1;
        long previousModified = -1;
        int stable = 0;
        while (System.nanoTime() < deadline) {
            if (!Files.isRegularFile(path) || !readable(path)) {
                stable = 0;
            } else {
                long size = Files.size(path);
                long modified = Files.getLastModifiedTime(path).toMillis();
                if (size == previousSize && modified == previousModified) stable++;
                else stable = 1;
                previousSize = size;
                previousModified = modified;
                if (stable >= config.getStableChecks()) return;
            }
            try { Thread.sleep(config.getIntervalMs()); }
            catch (InterruptedException error) { Thread.currentThread().interrupt(); throw new IOException("Estabilização interrompida", error); }
        }
        throw new IOException("ARQUIVO_NAO_ESTABILIZOU");
    }

    private boolean readable(Path path) {
        try (var input = Files.newInputStream(path)) { input.read(); return true; }
        catch (IOException error) { return false; }
    }
}
