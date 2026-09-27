package com.metalurgica.sistemagestaoindustrial.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SetorRequest(
        @NotBlank(message = "Código é obrigatório.") String codigo,
        @NotBlank(message = "Nome é obrigatório.") String nome,
        String descricao) {
}
