package com.metalurgica.sistemagestaoindustrial.quality;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record ProblemaQualidadeDados(
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
    public ProblemaQualidadeDados {
        if (detalhes == null || detalhes.isEmpty()) detalhes = Map.of();
        else detalhes = Collections.unmodifiableMap(new LinkedHashMap<>(detalhes));
    }
}
