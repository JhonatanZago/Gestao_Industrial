package com.metalurgica.sistemagestaoindustrial.dto.request;

import com.metalurgica.sistemagestaoindustrial.domain.BaseTempoPadrao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TempoPadraoProducaoRequest(
        @NotNull Long pecaId,
        @NotNull Long setorId,
        Long maquinaId,
        @NotNull @Positive BigDecimal segundos,
        @NotNull BaseTempoPadrao baseTempo,
        @Positive BigDecimal quantidadeReferencia,
        @NotNull LocalDate vigenciaInicio,
        LocalDate vigenciaFim) { }
