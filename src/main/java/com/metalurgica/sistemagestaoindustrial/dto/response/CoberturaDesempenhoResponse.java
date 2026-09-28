package com.metalurgica.sistemagestaoindustrial.dto.response;

import java.math.BigDecimal;

public record CoberturaDesempenhoResponse(long registrosTotais, long registrosComPadrao, long registrosSemPadrao,
                                          BigDecimal quantidadeComPadrao, BigDecimal percentual) { }
