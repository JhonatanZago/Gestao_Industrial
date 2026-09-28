package com.metalurgica.sistemagestaoindustrial.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RelatorioExecutivoResponse(Identificacao identificacao, ResumoProducaoResponse producao,
                                         ComparativoResponse comparativo, List<ProducaoAgrupadaResponse> setores,
                                         List<ProducaoAgrupadaResponse> funcionarios, List<ProducaoAgrupadaResponse> pecas,
                                         List<ProducaoAgrupadaResponse> maquinas, List<ProducaoAgrupadaResponse> turnos,
                                         List<ParadaPorMotivoResponse> paradas, List<ResumoMetaRelatorioResponse> metas,
                                         DesempenhoResponse desempenho, List<ResumoFinanceiroIndustrialResponse> financeiro,
                                         List<String> pontosAtencao, List<ComparativoFinanceiroResponse> comparativoFinanceiro,
                                         QualidadeDadosResumoResponse qualidadeDados) {
    public RelatorioExecutivoResponse(Identificacao identificacao, ResumoProducaoResponse producao,
                                      ComparativoResponse comparativo, List<ProducaoAgrupadaResponse> setores,
                                      List<ProducaoAgrupadaResponse> funcionarios, List<ProducaoAgrupadaResponse> pecas,
                                      List<ProducaoAgrupadaResponse> maquinas, List<ProducaoAgrupadaResponse> turnos,
                                      List<ParadaPorMotivoResponse> paradas, List<ResumoMetaRelatorioResponse> metas,
                                      DesempenhoResponse desempenho, List<ResumoFinanceiroIndustrialResponse> financeiro,
                                      List<String> pontosAtencao, List<ComparativoFinanceiroResponse> comparativoFinanceiro) {
        this(identificacao, producao, comparativo, setores, funcionarios, pecas, maquinas, turnos, paradas, metas,
                desempenho, financeiro, pontosAtencao, comparativoFinanceiro, null);
    }
    public record Identificacao(Long empresaId, String empresaNome, LocalDateTime periodoInicio,
                                LocalDateTime periodoFim, OffsetDateTime geradoEm, String geradoPor) { }
}
