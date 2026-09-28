package com.metalurgica.sistemagestaoindustrial.dto.response;

import java.math.BigDecimal;

public record ParadaPorMotivoResponse(Long motivoId, String codigo, String nome, String categoria,
                                      long quantidadeOcorrencias, BigDecimal tempoParadoMinutos,
                                      BigDecimal percentualDoTempoParado) {
}
