package com.metalurgica.sistemagestaoindustrial.mapper;

import com.metalurgica.sistemagestaoindustrial.domain.MetaProducao;
import com.metalurgica.sistemagestaoindustrial.dto.response.MetaProducaoResponse;
import org.springframework.stereotype.Component;

@Component
public class MetaProducaoMapper {
    public MetaProducaoResponse toResponse(MetaProducao m) {
        return new MetaProducaoResponse(m.getId(), m.getEmpresa().getId(), m.getNome(), m.getDescricao(), m.getTipo(),
                m.getSetor() == null ? null : m.getSetor().getId(), m.getFuncionario() == null ? null : m.getFuncionario().getId(),
                m.getPeca() == null ? null : m.getPeca().getId(), m.getMaquina() == null ? null : m.getMaquina().getId(),
                m.getTurno() == null ? null : m.getTurno().getId(), m.getQuantidadeMeta(), m.getPeriodicidade(),
                m.getVigenciaInicio(), m.getVigenciaFim(), m.isAtivo(), m.getCreatedAt(), m.getUpdatedAt());
    }
}
