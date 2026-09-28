package com.metalurgica.sistemagestaoindustrial.assistente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record PerguntaAssistenteRequest(
        @NotBlank @Size(max = 2_000) String question,
        @Size(max = 100) @Pattern(regexp = "[A-Za-z0-9_-]{1,100}") String conversationId) {
}
