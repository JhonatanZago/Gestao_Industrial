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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityFilterChainIntegrationTests {

    @Autowired
    MockMvc mvc;

    @Test
    void rejectsProtectedResourceWithoutBearerToken() throws Exception {
        mvc.perform(get("/api/v1/empresas/1/setores"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void acceptsCorsPreflightOnlyForConfiguredOrigin() throws Exception {
        mvc.perform(options("/api/v1/auth/login")
                .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
            .andExpect(status().isOk());
    }

    @Test
    void rejectsMalformedBearerTokenWithSafeJson401() throws Exception {
        mvc.perform(get("/api/v1/empresas/1/setores")
                .header(HttpHeaders.AUTHORIZATION, "Bearer definitely-not-a-jwt"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.erro").value("Autenticação necessária"));
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
