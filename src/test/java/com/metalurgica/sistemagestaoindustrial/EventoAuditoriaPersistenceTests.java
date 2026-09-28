package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.audit.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventoAuditoriaPersistenceTests {
    @Test
    void keepsAuditEventContractWithNullableSnapshots() {
        var event = new EventoAuditoria(7L, null, null, TipoRecursoAuditoria.VALOR_PRODUCAO,
                42L, AcaoAuditoria.ALTERACAO, OrigemAuditoria.MANUAL, OffsetDateTime.now(),
                "{\"valor\":\"0.85\"}", "{\"valor\":\"0.90\"}",
                "{\"quantidade\":\"" + new BigDecimal("1.25") + "\"}", "corr-42");

        assertEquals(TipoRecursoAuditoria.VALOR_PRODUCAO, event.getTipoRecurso());
        assertEquals(AcaoAuditoria.ALTERACAO, event.getAcao());
        assertEquals(OrigemAuditoria.MANUAL, event.getOrigem());
        assertNull(event.getUsuarioId());
        assertTrue(event.getDadosAnteriores().contains("0.85"));
        assertTrue(event.getMetadados().contains("1.25"));
    }
}
