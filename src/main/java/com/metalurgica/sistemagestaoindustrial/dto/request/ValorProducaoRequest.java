package com.metalurgica.sistemagestaoindustrial.dto.request;

import com.metalurgica.sistemagestaoindustrial.domain.TipoValor;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ValorProducaoRequest(@NotNull Long pecaId, @NotNull Long setorId, Long maquinaId,
                                   @NotNull TipoValor tipoValor, @NotNull @Positive @Digits(integer = 13, fraction = 6) BigDecimal valorUnitario,
                                   @NotBlank @Size(min = 3, max = 3) String moeda, @NotNull LocalDate vigenciaInicio,
                                   LocalDate vigenciaFim) { }
