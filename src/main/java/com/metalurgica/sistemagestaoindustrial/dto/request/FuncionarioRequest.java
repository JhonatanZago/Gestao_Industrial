package com.metalurgica.sistemagestaoindustrial.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FuncionarioRequest(
        @NotBlank(message = "Código é obrigatório.") @Size(max = 50, message = "Código deve ter no máximo 50 caracteres.") String codigo,
        @NotBlank(message = "Nome é obrigatório.") @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres.") String nome) {
}
