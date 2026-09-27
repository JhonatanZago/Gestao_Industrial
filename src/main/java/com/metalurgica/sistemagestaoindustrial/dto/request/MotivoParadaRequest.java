package com.metalurgica.sistemagestaoindustrial.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record MotivoParadaRequest(
        @NotBlank(message = "Código é obrigatório.") @Size(max = 50) String codigo,
        @NotBlank(message = "Nome é obrigatório.") @Size(max = 200) String nome,
        @Size(max = 1000) String descricao, @Size(max = 50) String categoria) { }
