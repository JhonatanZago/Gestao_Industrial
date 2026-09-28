package com.metalurgica.sistemagestaoindustrial.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DesempenhoResponse(LocalDateTime inicio, LocalDateTime fim, BigDecimal quantidadeReal,
                                 BigDecimal quantidadeEsperada, BigDecimal desempenho, BigDecimal meta,
                                 BigDecimal atingimentoMeta, boolean metaDisponivel,
                                 CoberturaDesempenhoResponse cobertura) { }
