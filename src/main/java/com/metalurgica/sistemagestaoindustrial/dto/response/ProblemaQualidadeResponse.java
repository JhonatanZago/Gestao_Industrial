package com.metalurgica.sistemagestaoindustrial.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.metalurgica.sistemagestaoindustrial.quality.CategoriaQualidadeDados;
import com.metalurgica.sistemagestaoindustrial.quality.SeveridadeQualidadeDados;
import com.metalurgica.sistemagestaoindustrial.quality.TipoRecursoQualidadeDados;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemaQualidadeResponse(
        String codigo,
        CategoriaQualidadeDados categoria,
        SeveridadeQualidadeDados severidade,
        String mensagem,
        Long empresaId,
        TipoRecursoQualidadeDados tipoRecurso,
        Long recursoId,
        Long setorId,
        LocalDateTime dataReferencia,
        Map<String, Object> detalhes) {
}
