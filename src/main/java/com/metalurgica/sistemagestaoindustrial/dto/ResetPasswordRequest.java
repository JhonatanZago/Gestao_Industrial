package com.metalurgica.sistemagestaoindustrial.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(@NotBlank String novaSenha) {}
