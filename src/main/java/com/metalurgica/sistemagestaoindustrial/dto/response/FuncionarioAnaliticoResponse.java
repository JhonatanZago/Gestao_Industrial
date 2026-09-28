package com.metalurgica.sistemagestaoindustrial.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FuncionarioAnaliticoResponse(Long funcionarioId, String codigo, String nome,
                                           LocalDateTime inicio, LocalDateTime fim,
                                           BigDecimal quantidadeProduzida, long quantidadeRegistros,
                                           BigDecimal duracaoBrutaMinutos, BigDecimal tempoParadoMinutos,
                                           BigDecimal tempoProdutivoMinutos, BigDecimal taxaProducaoBruta,
                                           BigDecimal taxaProducaoProdutiva, BigDecimal percentualTempoParado,
                                           List<ProducaoAgrupadaResponse> pecas,
                                           List<ProducaoAgrupadaResponse> setores) {
}
