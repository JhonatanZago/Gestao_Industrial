package com.metalurgica.sistemagestaoindustrial.assistente.tool;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AiToolContractTests {
    @Test
    void definicaoExigeToolReadOnlyComTimeout() {
        assertThrows(IllegalArgumentException.class, () -> new AiToolDefinition(
                "x", "1", "descrição", Map.of(), Set.of(), true, Duration.ZERO, "ERP"));
    }

    @Test
    void requestNaoAceitaChamadaZero() {
        assertThrows(IllegalArgumentException.class, () -> new AiToolRequest("x", Map.of(), null, null, 0));
    }
}
