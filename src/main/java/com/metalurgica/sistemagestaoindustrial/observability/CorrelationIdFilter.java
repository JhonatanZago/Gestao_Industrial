package com.metalurgica.sistemagestaoindustrial.observability;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
@Component @Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(CorrelationIdFilter.class);
    private static final Pattern VALID = Pattern.compile("[A-Za-z0-9._-]{1,100}");
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        long started = System.nanoTime();
        String id = normalize(request.getHeader(CorrelationContext.HEADER));
        request.setAttribute(CorrelationContext.ATTRIBUTE, id); response.setHeader(CorrelationContext.HEADER, id); MDC.put(CorrelationContext.MDC_KEY, id);
        try { chain.doFilter(request, response); }
        finally {
            Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            String route = pattern == null ? routeClass(request.getRequestURI()) : pattern.toString();
            String duration = Long.toString((System.nanoTime() - started) / 1_000_000);
            MDC.put("http.method", request.getMethod()); MDC.put("http.route", LogSanitizer.safe(route));
            MDC.put("http.status", Integer.toString(response.getStatus())); MDC.put("durationMs", duration);
            LOG.info("http_request method={} path={} status={} durationMs={}", request.getMethod(), LogSanitizer.safe(route), response.getStatus(), duration);
            MDC.remove("http.method"); MDC.remove("http.route"); MDC.remove("http.status"); MDC.remove("durationMs");
            MDC.remove(CorrelationContext.MDC_KEY);
        }
    }
    static String normalize(String candidate) { return candidate != null && VALID.matcher(candidate).matches() ? candidate : UUID.randomUUID().toString(); }
    static String routeClass(String uri) { return uri == null || uri.isBlank() ? "/" : uri.replaceAll("/(?:(?:[0-9]+)|(?:[0-9a-fA-F]{8}-[0-9a-fA-F-]{27,}))(?=/|$)", "/{id}"); }
}
