package com.metalurgica.sistemagestaoindustrial.importacao;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

public interface FileChangeDetector extends AutoCloseable {
    void start(Consumer<FileChangeEvent> listener) throws IOException;
    void stop() throws IOException;
    @Override default void close() throws IOException { stop(); }

    record FileChangeEvent(Path path, Kind kind) {
        public enum Kind { CREATE, MODIFY, DELETE }
    }
}
