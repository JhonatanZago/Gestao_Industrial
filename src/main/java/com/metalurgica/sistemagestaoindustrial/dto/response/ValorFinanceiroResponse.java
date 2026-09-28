package com.metalurgica.sistemagestaoindustrial.dto.response;

import com.metalurgica.sistemagestaoindustrial.domain.TipoValor;
import java.math.BigDecimal;

public record ValorFinanceiroResponse(Long id, String codigo, String nome, String moeda, TipoValor tipoValor,
                                      BigDecimal quantidadeProduzida, BigDecimal valorTotalCalculado,
                                      BigDecimal valorPorHoraBruta, BigDecimal valorPorHoraProdutiva,
                                      CoberturaFinanceiraResponse cobertura) { }
