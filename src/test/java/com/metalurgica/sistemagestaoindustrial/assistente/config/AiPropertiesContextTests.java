package com.metalurgica.sistemagestaoindustrial.assistente.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class AiPropertiesContextTests {
    @Autowired
    private AiProperties properties;

    @Test
    void contextoDeTesteMantemIADesabilitadaEComLimitesPadrao() {
        assertFalse(properties.isEnabled());
        assertEquals(2_000, properties.getMaxQuestionLength());
        assertEquals(8, properties.getMaxToolCalls());
        assertEquals(30, properties.getMaxRequestsPerMinute());
    }
}
