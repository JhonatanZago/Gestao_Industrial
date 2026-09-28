package com.metalurgica.sistemagestaoindustrial.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResumoProducaoResponse(LocalDateTime inicio, LocalDateTime fim, BigDecimal quantidadeProduzida,
                                     long quantidadeRegistros, BigDecimal duracaoBrutaMinutos,
                                     BigDecimal tempoParadoMinutos, BigDecimal tempoProdutivoMinutos,
                                     BigDecimal taxaProducaoBruta, BigDecimal taxaProducaoProdutiva,
                                     BigDecimal percentualTempoParado,
                                     QualidadeDadosResumoResponse qualidadeDados) {
    public ResumoProducaoResponse(LocalDateTime inicio, LocalDateTime fim, BigDecimal quantidadeProduzida,
                                  long quantidadeRegistros, BigDecimal duracaoBrutaMinutos,
                                  BigDecimal tempoParadoMinutos, BigDecimal tempoProdutivoMinutos,
                                  BigDecimal taxaProducaoBruta, BigDecimal taxaProducaoProdutiva,
                                  BigDecimal percentualTempoParado) {
        this(inicio, fim, quantidadeProduzida, quantidadeRegistros, duracaoBrutaMinutos, tempoParadoMinutos,
                tempoProdutivoMinutos, taxaProducaoBruta, taxaProducaoProdutiva, percentualTempoParado, null);
    }
}
