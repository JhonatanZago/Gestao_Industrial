package com.metalurgica.sistemagestaoindustrial.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record RelatorioParadasResponse(BigDecimal tempoParadoTotal, BigDecimal percentualTempoParado,
                                       long quantidadeOcorrencias, List<ParadaPorMotivoResponse> principaisMotivos) { }
