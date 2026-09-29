package com.metalurgica.sistemagestaoindustrial.observability;
import java.util.regex.Pattern;
public final class LogSanitizer {
    private static final Pattern CONTROL = Pattern.compile("[\\r\\n\\t\\p{Cntrl}]");
    private static final Pattern SECRET = Pattern.compile("(?i)(bearer\\s+[a-z0-9._-]+|authorization|password|senha|api[-_ ]?key|refresh[-_ ]?token|cookie|secret)");
    private LogSanitizer() { }
    public static String safe(String value) {
        if (value == null) return "unknown";
        String normalized = CONTROL.matcher(value).replaceAll("_");
        if (SECRET.matcher(normalized).find()) return "[REDACTED]";
        return normalized.substring(0, Math.min(normalized.length(), 200));
    }
}
