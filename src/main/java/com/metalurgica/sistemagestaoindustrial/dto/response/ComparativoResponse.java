package com.metalurgica.sistemagestaoindustrial.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ComparativoResponse(LocalDateTime inicioAtual, LocalDateTime fimAtual,
                                  LocalDateTime inicioAnterior, LocalDateTime fimAnterior,
                                  ComparativoIndicador quantidadeProduzida,
                                  ComparativoIndicador quantidadeRegistros,
                                  ComparativoIndicador duracaoBrutaMinutos,
                                  ComparativoIndicador tempoParadoMinutos,
                                  ComparativoIndicador tempoProdutivoMinutos) {
    public record ComparativoIndicador(BigDecimal valorAtual, BigDecimal valorAnterior,
                                       BigDecimal variacaoAbsoluta, BigDecimal variacaoPercentual) {
    }
}
