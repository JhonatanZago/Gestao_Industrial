package com.metalurgica.sistemagestaoindustrial;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"operations.max-payload-bytes=32",
        "spring.datasource.url=jdbc:h2:mem:payloadlimit;MODE=PostgreSQL;DB_CLOSE_DELAY=0;DATABASE_TO_LOWER=TRUE"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PayloadLimitIntegrationTests {
    @Autowired MockMvc mvc;

    @Test
    void rejectsKnownJsonPayloadBeforeController() throws Exception {
        String oversized = "{\"email\":\"long-address@example.test\",\"senha\":\"long-password\"}";
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .header("X-Correlation-Id", "payload-json-1").content(oversized))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(header().string("X-Correlation-Id", "payload-json-1"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
