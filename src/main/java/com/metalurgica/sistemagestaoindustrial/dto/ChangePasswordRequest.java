package com.metalurgica.sistemagestaoindustrial.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(@NotBlank String senhaAtual, @NotBlank String novaSenha) {}
