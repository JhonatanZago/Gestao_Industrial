package com.metalurgica.sistemagestaoindustrial.mapper;

import com.metalurgica.sistemagestaoindustrial.domain.Funcionario;
import com.metalurgica.sistemagestaoindustrial.dto.response.FuncionarioResponse;
import org.springframework.stereotype.Component;

@Component
public class FuncionarioMapper {
    public FuncionarioResponse toResponse(Funcionario f) {
        return new FuncionarioResponse(f.getId(), f.getEmpresa().getId(), f.getCodigo(), f.getNome(), f.isAtivo(), f.getCreatedAt(), f.getUpdatedAt());
    }
}
