package com.metalurgica.sistemagestaoindustrial.dto.response;

import com.metalurgica.sistemagestaoindustrial.domain.TipoValor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResumoFinanceiroIndustrialResponse(LocalDateTime inicio, LocalDateTime fim, String moeda, TipoValor tipoValor,
                                                  BigDecimal quantidadeProduzida, BigDecimal valorTotalCalculado,
                                                  BigDecimal valorPorHoraBruta, BigDecimal valorPorHoraProdutiva,
                                                  CoberturaFinanceiraResponse cobertura) { }
