package com.metalurgica.sistemagestaoindustrial.mapper;

import com.metalurgica.sistemagestaoindustrial.domain.ValorProducao;
import com.metalurgica.sistemagestaoindustrial.dto.response.ValorProducaoResponse;
import org.springframework.stereotype.Component;

@Component
public class ValorProducaoMapper {
    public ValorProducaoResponse toResponse(ValorProducao v) {
        return new ValorProducaoResponse(v.getId(), v.getEmpresa().getId(), v.getPeca().getId(), v.getSetor().getId(),
                v.getMaquina() == null ? null : v.getMaquina().getId(), v.getTipoValor(), v.getValorUnitario(), v.getMoeda(),
                v.getVigenciaInicio(), v.getVigenciaFim(), v.isAtivo(), v.getCreatedAt(), v.getUpdatedAt());
    }
}
