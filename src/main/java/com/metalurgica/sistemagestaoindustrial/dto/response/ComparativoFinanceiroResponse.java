package com.metalurgica.sistemagestaoindustrial.dto.response;

import com.metalurgica.sistemagestaoindustrial.domain.TipoValor;
import java.math.BigDecimal;

public record ComparativoFinanceiroResponse(String moeda, TipoValor tipoValor, BigDecimal valorAtual,
                                           BigDecimal valorAnterior, BigDecimal variacaoAbsoluta,
                                           BigDecimal variacaoPercentual) { }
