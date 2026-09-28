package com.metalurgica.sistemagestaoindustrial.dto.request;

import com.metalurgica.sistemagestaoindustrial.audit.AcaoAuditoria;
import com.metalurgica.sistemagestaoindustrial.audit.OrigemAuditoria;
import com.metalurgica.sistemagestaoindustrial.audit.TipoRecursoAuditoria;

import java.time.OffsetDateTime;

public record AuditoriaFiltro(OffsetDateTime inicio, OffsetDateTime fim, Long usuarioId,
                              TipoRecursoAuditoria tipoRecurso, Long recursoId,
                              AcaoAuditoria acao, OrigemAuditoria origem,
                              String correlationId) {
    public AuditoriaFiltro {
        if (inicio != null && fim != null && fim.isBefore(inicio)) {
            throw new IllegalArgumentException("O fim da auditoria deve ser posterior ao inicio.");
        }
        if (correlationId != null && correlationId.length() > 100) {
            throw new IllegalArgumentException("correlationId excede o limite permitido.");
        }
    }
}
