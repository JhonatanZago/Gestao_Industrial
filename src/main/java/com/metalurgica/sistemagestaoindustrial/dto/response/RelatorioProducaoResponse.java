package com.metalurgica.sistemagestaoindustrial.dto.response;

import java.util.List;

public record RelatorioProducaoResponse(ResumoProducaoResponse resumo, List<ProducaoAgrupadaResponse> setores,
                                        List<ProducaoAgrupadaResponse> funcionarios, List<ProducaoAgrupadaResponse> pecas,
                                        List<ProducaoAgrupadaResponse> maquinas, List<ProducaoAgrupadaResponse> turnos) { }
