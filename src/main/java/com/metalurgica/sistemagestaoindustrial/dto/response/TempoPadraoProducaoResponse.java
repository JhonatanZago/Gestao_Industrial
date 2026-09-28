package com.metalurgica.sistemagestaoindustrial.dto.response;

import com.metalurgica.sistemagestaoindustrial.domain.BaseTempoPadrao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record TempoPadraoProducaoResponse(Long id, Long empresaId, Long pecaId, Long setorId, Long maquinaId,
                                           BigDecimal segundos, BaseTempoPadrao baseTempo, BigDecimal quantidadeReferencia,
                                           LocalDate vigenciaInicio, LocalDate vigenciaFim, boolean ativo,
                                           OffsetDateTime createdAt, OffsetDateTime updatedAt) { }
