package com.metalurgica.sistemagestaoindustrial.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record ImportacaoAutomaticaStatusResponse(
        boolean enabled, boolean monitoramentoAtivo, String status,
        OffsetDateTime ultimaVerificacao, ImportacaoResumo ultimaImportacao,
        Reconciliacao reconciliacao, List<Fonte> fontes) {
    public record Reconciliacao(boolean enabled, String cron, String timezone) { }
    public record Fonte(Long setorId, String setor, boolean habilitada, boolean acessivel,
                        String status, OffsetDateTime ultimoCheck, ImportacaoResumo ultimaImportacao) { }
    public record ImportacaoResumo(Long id, String arquivo, String origem, String resultado,
                                   OffsetDateTime iniciadoEm, OffsetDateTime finalizadoEm,
                                   int linhasLidas, int linhasImportadas, int linhasIgnoradas) { }
}
