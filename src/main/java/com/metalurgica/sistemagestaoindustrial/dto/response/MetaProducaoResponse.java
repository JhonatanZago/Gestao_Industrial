package com.metalurgica.sistemagestaoindustrial.dto.response;

import com.metalurgica.sistemagestaoindustrial.domain.PeriodicidadeMeta;
import com.metalurgica.sistemagestaoindustrial.domain.TipoMeta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record MetaProducaoResponse(Long id, Long empresaId, String nome, String descricao, TipoMeta tipo,
                                   Long setorId, Long funcionarioId, Long pecaId, Long maquinaId, Long turnoId,
                                   BigDecimal quantidadeMeta, PeriodicidadeMeta periodicidade,
                                   LocalDate vigenciaInicio, LocalDate vigenciaFim, boolean ativo,
                                   OffsetDateTime createdAt, OffsetDateTime updatedAt) { }
