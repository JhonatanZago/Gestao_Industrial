package com.metalurgica.sistemagestaoindustrial.mapper;
import com.metalurgica.sistemagestaoindustrial.domain.Maquina;
import com.metalurgica.sistemagestaoindustrial.dto.response.MaquinaResponse;
import org.springframework.stereotype.Component;
@Component public class MaquinaMapper { public MaquinaResponse toResponse(Maquina m) { return new MaquinaResponse(m.getId(), m.getEmpresa().getId(), m.getSetor().getId(), m.getSetor().getCodigo(), m.getSetor().getNome(), m.getCodigo(), m.getNome(), m.getDescricao(), m.getTipo(), m.isAtivo(), m.getCreatedAt(), m.getUpdatedAt()); } }
