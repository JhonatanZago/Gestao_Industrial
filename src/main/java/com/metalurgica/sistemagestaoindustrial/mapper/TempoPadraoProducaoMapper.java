package com.metalurgica.sistemagestaoindustrial.mapper;

import com.metalurgica.sistemagestaoindustrial.domain.TempoPadraoProducao;
import com.metalurgica.sistemagestaoindustrial.dto.response.TempoPadraoProducaoResponse;
import org.springframework.stereotype.Component;

@Component
public class TempoPadraoProducaoMapper {
    public TempoPadraoProducaoResponse toResponse(TempoPadraoProducao t) {
        return new TempoPadraoProducaoResponse(t.getId(), t.getEmpresa().getId(), t.getPeca().getId(),
                t.getSetor().getId(), t.getMaquina() == null ? null : t.getMaquina().getId(), t.getSegundos(),
                t.getBaseTempo(), t.getQuantidadeReferencia(), t.getVigenciaInicio(), t.getVigenciaFim(),
                t.isAtivo(), t.getCreatedAt(), t.getUpdatedAt());
    }
}
