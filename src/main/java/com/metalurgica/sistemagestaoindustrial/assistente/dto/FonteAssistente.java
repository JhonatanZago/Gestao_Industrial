package com.metalurgica.sistemagestaoindustrial.assistente.dto;

import java.time.LocalDateTime;

public record FonteAssistente(
        String tipo,
        String identificador,
        String titulo,
        LocalDateTime inicio,
        LocalDateTime fim) {
}
