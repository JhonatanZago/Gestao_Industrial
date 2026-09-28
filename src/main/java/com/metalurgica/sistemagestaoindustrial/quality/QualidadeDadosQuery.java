package com.metalurgica.sistemagestaoindustrial.quality;

import com.metalurgica.sistemagestaoindustrial.analytics.AnalyticsQuery;

import java.time.LocalDateTime;

public record QualidadeDadosQuery(
        LocalDateTime inicio,
        LocalDateTime fim,
        Long setorId,
        Long funcionarioId,
        Long pecaId,
        Long maquinaId,
        Long turnoId,
        CategoriaQualidadeDados categoria,
        SeveridadeQualidadeDados severidade,
        String codigo,
        TipoRecursoQualidadeDados tipoRecurso,
        Long recursoId) {
    public AnalyticsQuery analyticsQuery() {
        return new AnalyticsQuery(inicio, fim, setorId, funcionarioId, pecaId, maquinaId, turnoId);
    }
}
