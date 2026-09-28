package com.metalurgica.sistemagestaoindustrial.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DuplicidadeProducaoResponse(
        String grupoDuplicidade,
        Long empresaId,
        Long setorId,
        Long funcionarioId,
        Long pecaId,
        Long maquinaId,
        Long turnoId,
        LocalDateTime inicio,
        LocalDateTime fim,
        BigDecimal quantidade,
        List<Long> registrosRelacionados) {
}
