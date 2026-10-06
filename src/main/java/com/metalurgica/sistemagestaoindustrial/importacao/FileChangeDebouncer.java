package com.metalurgica.sistemagestaoindustrial.importacao;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

public final class FileChangeDebouncer implements AutoCloseable {
    private final ScheduledExecutorService executor;
    private final Map<Path, ScheduledFuture<?>> pending = new ConcurrentHashMap<>();
    private final long delayMs;
    private final Consumer<Path> callback;

    public FileChangeDebouncer(long delayMs, Consumer<Path> callback) {
        this.delayMs = delayMs; this.callback = callback;
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            var thread = new Thread(r, "excel-file-debounce"); thread.setDaemon(true); return thread;
        });
    }

    public void signal(Path path) {
        var normalized = path.toAbsolutePath().normalize();
        var previous = pending.remove(normalized);
        if (previous != null) previous.cancel(false);
        pending.put(normalized, executor.schedule(() -> {
            pending.remove(normalized);
            callback.accept(normalized);
        }, Math.max(1, delayMs), TimeUnit.MILLISECONDS));
    }

    @Override public void close() {
        pending.values().forEach(future -> future.cancel(false));
        pending.clear(); executor.shutdownNow();
    }
}
