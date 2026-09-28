package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.audit.AcaoAuditoria;
import com.metalurgica.sistemagestaoindustrial.audit.OrigemAuditoria;
import com.metalurgica.sistemagestaoindustrial.audit.TipoRecursoAuditoria;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditoriaEnumsTests {
    @Test
    void keepsStableAuditVocabulary() {
        assertTrue(java.util.Set.of(TipoRecursoAuditoria.values()).contains(TipoRecursoAuditoria.REGISTRO_PRODUCAO));
        assertTrue(java.util.Set.of(TipoRecursoAuditoria.values()).contains(TipoRecursoAuditoria.IMPORTACAO));
        assertTrue(java.util.Set.of(AcaoAuditoria.values()).contains(AcaoAuditoria.ALTERACAO_SENHA));
        assertTrue(java.util.Set.of(AcaoAuditoria.values()).contains(AcaoAuditoria.ALTERACAO_PERMISSAO));
        assertTrue(java.util.Set.of(OrigemAuditoria.values()).containsAll(java.util.Set.of(
                OrigemAuditoria.MANUAL, OrigemAuditoria.IMPORTACAO_EXCEL,
                OrigemAuditoria.SISTEMA, OrigemAuditoria.API)));
    }
}
