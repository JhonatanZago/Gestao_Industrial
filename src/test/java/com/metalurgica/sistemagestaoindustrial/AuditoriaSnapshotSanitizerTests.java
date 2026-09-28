package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.audit.AuditoriaSnapshotSanitizer;
import com.metalurgica.sistemagestaoindustrial.audit.TipoRecursoAuditoria;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuditoriaSnapshotSanitizerTests {
    private final AuditoriaSnapshotSanitizer sanitizer = new AuditoriaSnapshotSanitizer();

    @Test
    void removesSensitiveKeysRecursivelyAndKeepsBusinessValues() {
        var json = sanitizer.sanitize(TipoRecursoAuditoria.USUARIO, Map.of(
                "nome", "Operador Ágil", "senhaHash", "hash", "metadados", Map.of("token", "jwt", "status", "ATIVO")));

        assertTrue(json.contains("Operador Ágil"));
        assertTrue(json.contains("ATIVO"));
        assertFalse(json.contains("senhaHash"));
        assertFalse(json.contains("token"));
        assertFalse(json.contains("jwt"));
    }

    @Test
    void rejectsOversizedSnapshot() {
        var oversized = Map.of("observacao", "x".repeat(40_000));
        assertThrows(IllegalArgumentException.class,
                () -> sanitizer.sanitize(TipoRecursoAuditoria.REGISTRO_PRODUCAO, oversized));
    }
}
