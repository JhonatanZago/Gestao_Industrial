package com.metalurgica.sistemagestaoindustrial.analytics;

import java.time.LocalDateTime;

public record AnalyticsQuery(LocalDateTime inicio, LocalDateTime fim, Long setorId, Long funcionarioId,
                             Long pecaId, Long maquinaId, Long turnoId) {
}
