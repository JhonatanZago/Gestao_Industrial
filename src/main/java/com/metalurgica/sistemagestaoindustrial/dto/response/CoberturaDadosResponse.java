package com.metalurgica.sistemagestaoindustrial.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CoberturaDadosResponse(
        long registrosTotais,
        long registrosCobertos,
        long registrosDescobertos,
        BigDecimal quantidadeCoberta,
        BigDecimal quantidadeDescoberta,
        BigDecimal percentualCobertura) {
}
