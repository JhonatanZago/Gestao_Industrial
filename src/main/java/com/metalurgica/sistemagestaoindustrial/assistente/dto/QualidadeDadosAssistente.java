package com.metalurgica.sistemagestaoindustrial.assistente.dto;

public record QualidadeDadosAssistente(
        String status,
        long alertas,
        long alertasCriticos,
        String cobertura) {
}
