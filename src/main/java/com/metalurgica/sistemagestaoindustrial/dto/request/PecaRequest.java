package com.metalurgica.sistemagestaoindustrial.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PecaRequest(
        @NotBlank(message = "Código é obrigatório.") @Size(max = 80, message = "Código deve ter no máximo 80 caracteres.") String codigo,
        @NotBlank(message = "Descrição é obrigatória.") @Size(max = 300, message = "Descrição deve ter no máximo 300 caracteres.") String descricao) {
}
