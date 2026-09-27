package com.metalurgica.sistemagestaoindustrial.mapper;

import com.metalurgica.sistemagestaoindustrial.domain.Empresa;
import com.metalurgica.sistemagestaoindustrial.dto.request.EmpresaRequest;
import com.metalurgica.sistemagestaoindustrial.dto.response.EmpresaResponse;
import org.springframework.stereotype.Component;

@Component
public class EmpresaMapper {
    public EmpresaResponse toResponse(Empresa e) {
        return new EmpresaResponse(e.getId(), e.getRazaoSocial(), e.getNomeFantasia(), e.getCnpj(),
                e.isAtivo(), e.getCreatedAt(), e.getUpdatedAt());
    }

    public void update(Empresa e, EmpresaRequest request) {
        e.setRazaoSocial(request.razaoSocial());
        e.setNomeFantasia(request.nomeFantasia());
        e.setCnpj(request.cnpj());
    }
}
