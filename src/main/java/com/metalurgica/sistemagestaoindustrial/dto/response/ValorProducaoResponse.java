package com.metalurgica.sistemagestaoindustrial.dto.response;

import com.metalurgica.sistemagestaoindustrial.domain.TipoValor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ValorProducaoResponse(Long id, Long empresaId, Long pecaId, Long setorId, Long maquinaId,
                                    TipoValor tipoValor, BigDecimal valorUnitario, String moeda,
                                    LocalDate vigenciaInicio, LocalDate vigenciaFim, boolean ativo,
                                    OffsetDateTime createdAt, OffsetDateTime updatedAt) { }
