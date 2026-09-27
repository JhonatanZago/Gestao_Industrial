package com.metalurgica.sistemagestaoindustrial.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
public record MaquinaRequest(@NotNull Long setorId, @NotBlank @Size(max = 50) String codigo, @NotBlank @Size(max = 200) String nome, @Size(max = 1000) String descricao, @Size(max = 50) String tipo) { }
