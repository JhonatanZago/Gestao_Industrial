package com.metalurgica.sistemagestaoindustrial.assistente.security;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssistenteSecurityContextTests {
    @Test
    void contextoEImutavelEAplicaPermissoes() {
        var context = new AssistenteSecurityContext(7L, 3L, "OPERADOR",
                Set.of("ANALYTICS_VISUALIZAR"), Set.of(10L), false);
        assertTrue(context.hasPermission("ANALYTICS_VISUALIZAR"));
        assertTrue(context.canAccessSector(10L));
        assertFalse(context.canAccessSector(11L));
        assertThrowsUnsupported(context.permissions());
    }

    private void assertThrowsUnsupported(Set<String> values) {
        try {
            values.add("OUTRO");
            throw new AssertionError("Conjunto deveria ser imutável");
        } catch (UnsupportedOperationException expected) {
            // esperado
        }
    }
}
