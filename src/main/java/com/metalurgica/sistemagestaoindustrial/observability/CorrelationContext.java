package com.metalurgica.sistemagestaoindustrial.observability;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
public final class CorrelationContext {
    public static final String HEADER = "X-Correlation-Id";
    public static final String ATTRIBUTE = CorrelationContext.class.getName() + ".id";
    public static final String MDC_KEY = "correlationId";
    private CorrelationContext() { }
    public static String from(HttpServletRequest request) {
        Object value = request == null ? null : request.getAttribute(ATTRIBUTE);
        if (value instanceof String id && !id.isBlank()) return id;
        String mdc = MDC.get(MDC_KEY);
        return mdc == null || mdc.isBlank() ? "unknown" : mdc;
    }
    public static String current() {
        String value = MDC.get(MDC_KEY);
        return value == null || value.isBlank() ? "unknown" : value;
    }
}
