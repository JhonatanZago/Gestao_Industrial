package com.metalurgica.sistemagestaoindustrial.assistente.dto;

import java.time.LocalDateTime;

public record PeriodoAssistente(LocalDateTime inicio, LocalDateTime fim) {
    public PeriodoAssistente {
        if (inicio == null || fim == null || fim.isBefore(inicio)) {
            throw new IllegalArgumentException("Período do assistente inválido.");
        }
    }
}
