package com.metalurgica.sistemagestaoindustrial.dto.response;

import java.math.BigDecimal;

public record ResumoMetaRelatorioResponse(MetaProducaoResponse meta, BigDecimal valorReal,
                                           BigDecimal atingimentoPercentual, String statusCalculado) { }
