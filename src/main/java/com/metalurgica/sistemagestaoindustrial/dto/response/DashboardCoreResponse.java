package com.metalurgica.sistemagestaoindustrial.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DashboardCoreResponse(
        LocalDateTime inicio,
        LocalDateTime fim,
        BigDecimal quantidadeProduzida,
        BigDecimal duracaoRegistradaMinutos,
        long setoresComDados,
        long funcionariosEnvolvidos,
        long codigosPecaProduzidos,
        List<ProducaoAgrupadaResponse> porSetor,
        List<ProducaoAgrupadaResponse> porFuncionario,
        List<ProducaoAgrupadaResponse> porPeca,
        List<SerieTemporalResponse> evolucao) {
}
