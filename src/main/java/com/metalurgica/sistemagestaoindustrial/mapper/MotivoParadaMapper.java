package com.metalurgica.sistemagestaoindustrial.mapper;
import com.metalurgica.sistemagestaoindustrial.domain.MotivoParada;
import com.metalurgica.sistemagestaoindustrial.dto.response.MotivoParadaResponse;
import org.springframework.stereotype.Component;
@Component public class MotivoParadaMapper {
    public MotivoParadaResponse toResponse(MotivoParada m) { return new MotivoParadaResponse(m.getId(), m.getEmpresa().getId(), m.getCodigo(), m.getNome(), m.getDescricao(), m.getCategoria(), m.isAtivo(), m.getCreatedAt(), m.getUpdatedAt()); }
}
