package com.metalurgica.sistemagestaoindustrial.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SerieTemporalResponse(LocalDate periodo, BigDecimal quantidadeProduzida, long quantidadeRegistros,
                                    BigDecimal duracaoBrutaMinutos, BigDecimal tempoParadoMinutos,
                                    BigDecimal tempoProdutivoMinutos, BigDecimal taxaProducaoBruta,
                                    BigDecimal taxaProducaoProdutiva, BigDecimal percentualTempoParado) {
}
