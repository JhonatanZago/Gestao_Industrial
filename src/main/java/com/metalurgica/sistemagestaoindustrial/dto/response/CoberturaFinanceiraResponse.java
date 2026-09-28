package com.metalurgica.sistemagestaoindustrial.dto.response;

import java.math.BigDecimal;

public record CoberturaFinanceiraResponse(long registrosTotais, long registrosComValor, long registrosSemValor,
                                          BigDecimal quantidadeComValor, BigDecimal valorTotalCalculado,
                                          BigDecimal coberturaValorPercentual) { }
