package com.metalurgica.sistemagestaoindustrial.importacao;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/** Política única de elegibilidade usada pelo watcher e pela reconciliação. */
public final class AutomaticImportFilePolicy {
    private AutomaticImportFilePolicy() { }

    public static Path watchRoot(AutomaticImportProperties.Directory source) {
        var path = Path.of(source.getPath()).toAbsolutePath().normalize();
        return source.getKind() == AutomaticImportProperties.SourceKind.FILE ? path.getParent() : path;
    }

    public static boolean eligible(Path file, AutomaticImportProperties.Directory source) {
        if (file == null || !Files.isRegularFile(file)) return false;
        var normalized = file.toAbsolutePath().normalize();
        if (source.getKind() == AutomaticImportProperties.SourceKind.FILE
                && !normalized.equals(Path.of(source.getPath()).toAbsolutePath().normalize())) return false;
        var name = normalized.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".xlsx") && !name.startsWith("~$") && !name.startsWith(".")
                && !name.endsWith(".tmp.xlsx") && !name.endsWith(".lock.xlsx");
    }
}
