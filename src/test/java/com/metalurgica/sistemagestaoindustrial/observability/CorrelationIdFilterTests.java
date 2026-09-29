package com.metalurgica.sistemagestaoindustrial.observability;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.*;
import static org.junit.jupiter.api.Assertions.*;
class CorrelationIdFilterTests {
    private final CorrelationIdFilter filter = new CorrelationIdFilter();
    @Test void validatesAndNormalizesIdsAndRoutes() {
        assertEquals("abc-123", CorrelationIdFilter.normalize("abc-123")); assertNotEquals("invalid value", CorrelationIdFilter.normalize("invalid value"));
        assertEquals("/api/v1/empresas/{id}/setores/{id}", CorrelationIdFilter.routeClass("/api/v1/empresas/123/setores/456"));
    }
    @Test void returnsHeaderAndClearsMdc() throws Exception {
        var request = new MockHttpServletRequest("GET", "/test"); request.addHeader(CorrelationContext.HEADER, "request-1"); var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> assertEquals("request-1", MDC.get(CorrelationContext.MDC_KEY)));
        assertEquals("request-1", response.getHeader(CorrelationContext.HEADER)); assertNull(MDC.get(CorrelationContext.MDC_KEY));
    }
}
