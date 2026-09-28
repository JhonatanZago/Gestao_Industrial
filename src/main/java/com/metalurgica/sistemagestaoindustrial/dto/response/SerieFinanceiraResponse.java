package com.metalurgica.sistemagestaoindustrial.dto.response;

import com.metalurgica.sistemagestaoindustrial.domain.TipoValor;
import java.math.BigDecimal;
import java.time.LocalDate;

public record SerieFinanceiraResponse(LocalDate periodo, String moeda, TipoValor tipoValor, BigDecimal quantidadeProduzida,
                                     BigDecimal valorTotalCalculado, CoberturaFinanceiraResponse cobertura) { }
