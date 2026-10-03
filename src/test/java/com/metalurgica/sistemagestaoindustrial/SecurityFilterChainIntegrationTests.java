package com.metalurgica.sistemagestaoindustrial;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityFilterChainIntegrationTests {

    @Autowired
    MockMvc mvc;

    @Test
    void rejectsProtectedResourceWithoutBearerToken() throws Exception {
        mvc.perform(get("/api/v1/empresas/1/setores"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("ACCESS_DENIED"))
            .andExpect(jsonPath("$.correlationId").isNotEmpty())
            .andExpect(header().exists("X-Correlation-Id"));
    }

    @Test
    void deniesUnclassifiedApiRouteWithoutInvokingBusinessCode() throws Exception {
        mvc.perform(get("/api/v1/nao-classificada"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void acceptsCorsPreflightOnlyForConfiguredOrigin() throws Exception {
        mvc.perform(options("/api/v1/auth/login")
                .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                    org.hamcrest.Matchers.containsString(HttpHeaders.CONTENT_DISPOSITION)));
    }

    @Test
    void emitsExplicitSecurityHeaders() throws Exception {
        mvc.perform(options("/api/v1/auth/login")
                .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(header().string("Referrer-Policy", "no-referrer"))
            .andExpect(header().string("X-Frame-Options", "DENY"))
            .andExpect(header().string("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'"));
    }

    @Test
    void rejectsMalformedBearerTokenWithSafeJson401() throws Exception {
        mvc.perform(get("/api/v1/empresas/1/setores")
                .header(HttpHeaders.AUTHORIZATION, "Bearer definitely-not-a-jwt"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Autenticação necessária."))
            .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void preservesValidCorrelationIdAndReplacesInvalidOne() throws Exception {
        mvc.perform(get("/api/v1/empresas/1/setores").header("X-Correlation-Id", "abc-123"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string("X-Correlation-Id", "abc-123"))
            .andExpect(jsonPath("$.correlationId").value("abc-123"));
        mvc.perform(get("/api/v1/empresas/1/setores").header("X-Correlation-Id", "invalid value"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string("X-Correlation-Id", org.hamcrest.Matchers.matchesPattern("[0-9a-f-]{36}")));
    }

    @Test
    void rejectsExpiredBearerToken() throws Exception {
        var key = new SecretKeySpec("test-only-secret-key-with-at-least-32-bytes".getBytes(), "HmacSHA256");
        var encoder = NimbusJwtEncoder.withSecretKey(key).build();
        var claims = JwtClaimsSet.builder().subject("1").issuedAt(Instant.now().minusSeconds(120)).expiresAt(Instant.now().minusSeconds(60)).build();
        var token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        mvc.perform(get("/api/v1/empresas/1/setores").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isUnauthorized());
    }
}
