package com.metalurgica.sistemagestaoindustrial.mapper;
import com.metalurgica.sistemagestaoindustrial.domain.ParadaProducao;
import com.metalurgica.sistemagestaoindustrial.dto.response.ParadaProducaoResponse;
import org.springframework.stereotype.Component;
import java.time.Duration;
@Component public class ParadaProducaoMapper {
    public ParadaProducaoResponse toResponse(ParadaProducao p) { return new ParadaProducaoResponse(p.getId(), p.getEmpresa().getId(), p.getRegistroProducao().getId(), p.getMotivoParada().getId(), p.getMotivoParada().getCodigo(), p.getMotivoParada().getNome(), p.getMaquina() == null ? null : p.getMaquina().getId(), p.getMaquina() == null ? null : p.getMaquina().getCodigo(), p.getMaquina() == null ? null : p.getMaquina().getNome(), p.getInicio(), p.getFim(), Duration.between(p.getInicio(), p.getFim()).toMinutes(), p.getObservacao(), p.isAtivo(), p.getCreatedAt(), p.getUpdatedAt()); }
}
