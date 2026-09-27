package com.metalurgica.sistemagestaoindustrial.dto.request;

import jakarta.validation.constraints.NotBlank;

public record EmpresaRequest(
        @NotBlank(message = "Razão social é obrigatória.") String razaoSocial,
        @NotBlank(message = "Nome fantasia é obrigatório.") String nomeFantasia,
        @NotBlank(message = "CNPJ é obrigatório.") String cnpj) {
}
