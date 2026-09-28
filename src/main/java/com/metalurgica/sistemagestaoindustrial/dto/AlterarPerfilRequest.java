package com.metalurgica.sistemagestaoindustrial.dto;

import jakarta.validation.constraints.NotBlank;

public record AlterarPerfilRequest(@NotBlank String perfil) { }
