package com.metalurgica.sistemagestaoindustrial.dto.request;

import com.metalurgica.sistemagestaoindustrial.domain.PeriodicidadeMeta;
import com.metalurgica.sistemagestaoindustrial.domain.TipoMeta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MetaProducaoRequest(@NotBlank @Size(max = 150) String nome, @Size(max = 1000) String descricao,
                                  @NotNull TipoMeta tipo, Long setorId, Long funcionarioId, Long pecaId,
                                  Long maquinaId, Long turnoId, @NotNull @Positive BigDecimal quantidadeMeta,
                                  @NotNull PeriodicidadeMeta periodicidade, @NotNull LocalDate vigenciaInicio,
                                  LocalDate vigenciaFim) { }
