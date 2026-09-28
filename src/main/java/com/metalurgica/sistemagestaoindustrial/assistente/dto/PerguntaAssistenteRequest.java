package com.metalurgica.sistemagestaoindustrial.assistente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PerguntaAssistenteRequest(
        @NotBlank @Size(max = 2_000) String question,
        @Size(max = 100) String conversationId) {
}
