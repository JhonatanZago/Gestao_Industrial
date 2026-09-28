package com.metalurgica.sistemagestaoindustrial.assistente.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiPropertiesTests {
    @Test
    void usaDefaultsSegurosSemCredencialExterna() {
        var properties = new AiProperties();

        assertFalse(properties.isEnabled());
        assertEquals(2_000, properties.getMaxQuestionLength());
        assertEquals(8, properties.getMaxToolCalls());
        assertEquals(1_000, properties.getMaxToolResults());
        assertEquals(Duration.ofSeconds(30), properties.getTimeout());
        assertEquals(1_000, properties.getMaxOutputTokens());
        assertEquals(8, properties.getMaxRetrievedDocuments());
        assertEquals(5L * 1024 * 1024, properties.getMaxDocumentBytes());
        assertEquals(30, properties.getMaxRequestsPerMinute());
        assertEquals(1, properties.getRetryAttempts());
        properties.validate();
    }

    @Test
    void rejeitaLimitesInseguros() {
        var properties = new AiProperties();
        properties.setMaxToolCalls(0);
        assertThrows(IllegalStateException.class, properties::validate);
    }
}
