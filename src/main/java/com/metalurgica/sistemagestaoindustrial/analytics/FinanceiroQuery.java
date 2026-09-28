package com.metalurgica.sistemagestaoindustrial.analytics;

import com.metalurgica.sistemagestaoindustrial.domain.TipoValor;
import java.time.LocalDateTime;

public record FinanceiroQuery(LocalDateTime inicio, LocalDateTime fim, Long setorId, Long funcionarioId,
                              Long pecaId, Long maquinaId, Long turnoId, String moeda, TipoValor tipoValor) {
    public TipoValor tipoEfetivo() { return tipoValor == null ? TipoValor.VALOR_PRODUCAO : tipoValor; }
}
