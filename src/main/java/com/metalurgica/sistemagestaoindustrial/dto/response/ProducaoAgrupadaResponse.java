package com.metalurgica.sistemagestaoindustrial.dto.response;

import java.math.BigDecimal;

public record ProducaoAgrupadaResponse(Long id, String codigo, String nome, String setorCodigo, String setorNome,
                                       BigDecimal quantidadeProduzida, long quantidadeRegistros,
                                       BigDecimal duracaoBrutaMinutos, BigDecimal tempoParadoMinutos,
                                       BigDecimal tempoProdutivoMinutos, BigDecimal taxaProducaoBruta,
                                       BigDecimal taxaProducaoProdutiva, BigDecimal percentualTempoParado) {
}
