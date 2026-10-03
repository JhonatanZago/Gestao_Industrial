package com.metalurgica.sistemagestaoindustrial.assistente.rag;

import com.metalurgica.sistemagestaoindustrial.assistente.config.AiProperties;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RagDocumentTests {
    @Test
    void chunkingEDeterministico() {
        var properties = new AiProperties();
        properties.setChunkSize(100);
        properties.setChunkOverlap(10);
        var chunker = new DeterministicChunker(properties);
        assertEquals(chunker.chunk("a".repeat(250)), chunker.chunk("a".repeat(250)));
    }

    @Test
    void rejeitaPdfSemExtractorSeguro() {
        var validator = new DocumentContentValidator(new AiProperties());
        assertThrows(RuntimeException.class, () -> validator.validateAndNormalize("manual.pdf", "application/pdf", "texto".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void aceitaTxtERejeitaScript() {
        var validator = new DocumentContentValidator(new AiProperties());
        assertEquals("manual", validator.validateAndNormalize("manual.txt", "text/plain", "manual".getBytes(StandardCharsets.UTF_8)));
        assertThrows(RuntimeException.class, () -> validator.validateAndNormalize("manual.txt", "text/plain", "<script>x</script>".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void rejeitaPathTraversalNoNomeDoDocumento() {
        var validator = new DocumentContentValidator(new AiProperties());
        assertThrows(RuntimeException.class, () -> validator.validateAndNormalize("../manual.txt", "text/plain", "manual".getBytes(StandardCharsets.UTF_8)));
        assertThrows(RuntimeException.class, () -> validator.validateAndNormalize("sub\\manual.txt", "text/plain", "manual".getBytes(StandardCharsets.UTF_8)));
    }
}
