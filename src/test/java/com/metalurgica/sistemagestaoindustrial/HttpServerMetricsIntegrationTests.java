package com.metalurgica.sistemagestaoindustrial;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:httpmetrics;MODE=PostgreSQL;DB_CLOSE_DELAY=0;DATABASE_TO_LOWER=TRUE")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(HttpServerMetricsIntegrationTests.FailureEndpoint.class)
class HttpServerMetricsIntegrationTests extends AuthenticatedIntegrationTest {
    @Autowired MeterRegistry registry;

    @Test
    void records2xx4xxAnd5xxWithNormalizedRoutes() throws Exception {
        rawMvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/operacao/not-found/123456789")).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/operacao/test-http-failure")).andExpect(status().isInternalServerError());

        var timers = registry.find("http.server.requests").timers();
        assertTrue(timers.stream().anyMatch(timer -> "200".equals(timer.getId().getTag("status"))));
        assertTrue(timers.stream().anyMatch(timer -> "404".equals(timer.getId().getTag("status"))));
        assertTrue(timers.stream().anyMatch(timer -> "500".equals(timer.getId().getTag("status"))));
        timers.forEach(timer -> {
            String uri = timer.getId().getTag("uri");
            assertFalse(uri != null && uri.contains("123456789"));
        });
    }

    @RestController
    static class FailureEndpoint {
        @GetMapping("/api/v1/operacao/test-http-failure")
        void fail() { throw new IllegalStateException("forced-test-error"); }
    }
}
