package com.metalurgica.sistemagestaoindustrial.mapper;

import com.metalurgica.sistemagestaoindustrial.domain.Setor;
import com.metalurgica.sistemagestaoindustrial.dto.response.SetorResponse;
import org.springframework.stereotype.Component;

@Component
public class SetorMapper {
    public SetorResponse toResponse(Setor s) {
        return new SetorResponse(s.getId(), s.getEmpresa().getId(), s.getCodigo(), s.getNome(), s.getDescricao(),
                s.isAtivo(), s.getCreatedAt(), s.getUpdatedAt());
    }
}
