package com.metalurgica.sistemagestaoindustrial.mapper;

import com.metalurgica.sistemagestaoindustrial.domain.Peca;
import com.metalurgica.sistemagestaoindustrial.dto.response.PecaResponse;
import org.springframework.stereotype.Component;

@Component
public class PecaMapper {
    public PecaResponse toResponse(Peca p) {
        return new PecaResponse(p.getId(), p.getEmpresa().getId(), p.getCodigo(), p.getDescricao(), p.isAtivo(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
