package com.metalurgica.sistemagestaoindustrial;
import com.metalurgica.sistemagestaoindustrial.domain.Empresa;
import org.springframework.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class OperationalHealthIntegrationTests extends AuthenticatedIntegrationTest {
    @Test void publicHealthAndProbesExposeOnlyAggregateStatus() throws Exception {
        rawMvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP")).andExpect(jsonPath("$.components").doesNotExist());
        rawMvc.perform(get("/actuator/health/liveness")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
        rawMvc.perform(get("/actuator/health/readiness")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
    }
    @Test void operationalEndpointsAreProtectedAndSensitiveEndpointsRemainAbsent() throws Exception {
        rawMvc.perform(get("/actuator/info")).andExpect(status().isUnauthorized());
        rawMvc.perform(get("/actuator/prometheus")).andExpect(status().isUnauthorized());
        mvc.perform(get("/actuator/info")).andExpect(status().isOk()).andExpect(jsonPath("$.app.name").value("SistemaGestaoIndustrial")).andExpect(jsonPath("$.app.version").isNotEmpty()).andExpect(jsonPath("$.app.commit").isNotEmpty()).andExpect(jsonPath("$.security").doesNotExist());
        mvc.perform(get("/actuator/metrics")).andExpect(status().isOk()).andExpect(jsonPath("$.names").isArray());
        mvc.perform(get("/actuator/prometheus")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("jvm_"))).andExpect(content().string(org.hamcrest.Matchers.containsString("hikaricp_")));
        mvc.perform(get("/actuator/env")).andExpect(status().isNotFound());
        mvc.perform(get("/actuator/beans")).andExpect(status().isNotFound());
        mvc.perform(get("/actuator/configprops")).andExpect(status().isNotFound());
        mvc.perform(get("/actuator/heapdump")).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/operacao/status")).andExpect(status().isOk()).andExpect(jsonPath("$.databaseAvailable").value(true)).andExpect(jsonPath("$.ai").value("DISABLED"));
    }
    @Test void userWithoutOperationalPermissionIsForbidden() throws Exception {
        var company = new Empresa(); company.setRazaoSocial("Health User"); company.setNomeFantasia("Health User"); company.setCnpj(String.format("88%012d", Math.abs(System.nanoTime() % 1000000000000L))); company.touchForCreate(); company = empresaRepository.save(company);
        var user = usuarioService.create(company, perfilRepository.findByCodigo("GESTOR").orElseThrow(), "Gestor Health", "health-" + company.getId() + "@example.test", "SenhaSegura123");
        rawMvc.perform(get("/actuator/metrics").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenService.issue(user))).andExpect(status().isForbidden());
    }
}
