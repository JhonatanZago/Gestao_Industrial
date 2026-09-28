package com.metalurgica.sistemagestaoindustrial.dto.response;

import java.util.List;

public record RelatorioDesempenhoResponse(DesempenhoResponse resumo, List<DesempenhoComparativoResponse> setores,
                                          List<DesempenhoComparativoResponse> funcionarios) { }
