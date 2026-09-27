package com.metalurgica.sistemagestaoindustrial.mapper;

import com.metalurgica.sistemagestaoindustrial.domain.RegistroProducao;
import com.metalurgica.sistemagestaoindustrial.dto.response.RegistroProducaoResponse;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RegistroProducaoMapper {
    public RegistroProducaoResponse toResponse(RegistroProducao r) { return toResponse(r, 0); }
    public RegistroProducaoResponse toResponse(RegistroProducao r, long tempoParado) {
        long bruto = Duration.between(r.getInicio(), r.getFim()).toMinutes();
        return new RegistroProducaoResponse(
                r.getId(), r.getEmpresa().getId(),
                r.getSetor().getId(), r.getSetor().getCodigo(), r.getSetor().getNome(),
                r.getFuncionario().getId(), r.getFuncionario().getCodigo(), r.getFuncionario().getNome(),
                r.getPeca().getId(), r.getPeca().getCodigo(), r.getPeca().getDescricao(),
                r.getQuantidade(), r.getInicio(), r.getFim(), bruto, tempoParado, Math.max(0, bruto - tempoParado),
                r.getMaquina() == null ? null : r.getMaquina().getId(), r.getMaquina() == null ? null : r.getMaquina().getCodigo(), r.getMaquina() == null ? null : r.getMaquina().getNome(),
                r.getTurno() == null ? null : r.getTurno().getId(), r.getTurno() == null ? null : r.getTurno().getCodigo(), r.getTurno() == null ? null : r.getTurno().getNome(),
                r.getObservacao(), r.isAtivo(), r.getCreatedAt(), r.getUpdatedAt());
    }
}
