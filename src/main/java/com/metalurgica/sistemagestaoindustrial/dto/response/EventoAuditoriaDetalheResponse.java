package com.metalurgica.sistemagestaoindustrial.dto.response;

import com.metalurgica.sistemagestaoindustrial.audit.AcaoAuditoria;
import com.metalurgica.sistemagestaoindustrial.audit.OrigemAuditoria;
import com.metalurgica.sistemagestaoindustrial.audit.TipoRecursoAuditoria;

import java.time.OffsetDateTime;
import java.util.List;

public record EventoAuditoriaDetalheResponse(Long id, Long empresaId, Long usuarioId,
                                             String usuarioNomeSnapshot, TipoRecursoAuditoria tipoRecurso,
                                             Long recursoId, AcaoAuditoria acao, OrigemAuditoria origem,
                                             OffsetDateTime dataHora, String dadosAnteriores,
                                             String dadosNovos, String metadados, String correlationId,
                                             List<AlteracaoCampoResponse> alteracoes) { }
