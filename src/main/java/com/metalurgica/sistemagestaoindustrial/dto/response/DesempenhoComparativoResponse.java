package com.metalurgica.sistemagestaoindustrial.dto.response;

import java.math.BigDecimal;

public record DesempenhoComparativoResponse(Long id, String codigo, String nome, BigDecimal quantidadeReal,
                                            BigDecimal quantidadeEsperada, BigDecimal desempenho, BigDecimal meta,
                                            BigDecimal atingimentoMeta, boolean metaDisponivel,
                                            CoberturaDesempenhoResponse cobertura) { }
