package com.metalurgica.sistemagestaoindustrial.importacao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class WatchServiceFileChangeDetector implements FileChangeDetector {
    private static final Logger LOG = LoggerFactory.getLogger(WatchServiceFileChangeDetector.class);
    private final Map<WatchKey, Path> directories = new ConcurrentHashMap<>();
    private final java.util.List<Path> configuredPaths;
    private volatile WatchService watchService;
    private volatile Thread thread;
    private volatile boolean running;

    public WatchServiceFileChangeDetector(Iterable<Path> paths) {
        configuredPaths = new java.util.ArrayList<>();
        for (Path path : paths) configuredPaths.add(path.toAbsolutePath().normalize());
    }

    @Override
    public synchronized void start(Consumer<FileChangeEvent> listener) throws IOException {
        if (running) return;
        watchService = FileSystems.getDefault().newWatchService();
        for (Path directory : configuredPaths) {
            if (!Files.isDirectory(directory)) throw new IOException("Diretório não acessível: " + directory);
            directories.put(directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE), directory);
        }
        running = true;
        thread = new Thread(() -> loop(listener), "excel-file-watcher");
        thread.setDaemon(true);
        thread.start();
    }

    private void loop(Consumer<FileChangeEvent> listener) {
        while (running) {
            try {
                WatchKey key = watchService.take();
                Path directory = directories.get(key);
                if (directory != null) for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) continue;
                    @SuppressWarnings("unchecked") var context = ((WatchEvent<Path>) event).context();
                    var kind = event.kind() == StandardWatchEventKinds.ENTRY_CREATE
                            ? FileChangeEvent.Kind.CREATE
                            : event.kind() == StandardWatchEventKinds.ENTRY_DELETE
                            ? FileChangeEvent.Kind.DELETE : FileChangeEvent.Kind.MODIFY;
                    listener.accept(new FileChangeEvent(directory.resolve(context), kind));
                }
                if (!key.reset()) directories.remove(key);
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                return;
            } catch (RuntimeException error) {
                if (running) LOG.warn("FILE_CHANGE_DETECTOR_ERROR", error);
            }
        }
    }

    @Override
    public synchronized void stop() throws IOException {
        running = false;
        if (thread != null) thread.interrupt();
        if (watchService != null) watchService.close();
        thread = null;
        watchService = null;
        directories.clear();
    }
}
