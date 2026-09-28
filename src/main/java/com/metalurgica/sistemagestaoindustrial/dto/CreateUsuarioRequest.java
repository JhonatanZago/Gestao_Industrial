package com.metalurgica.sistemagestaoindustrial.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUsuarioRequest(@NotBlank String nome, @NotBlank @Email String email,
                                   @NotBlank String senha, @NotBlank String perfil) {}
