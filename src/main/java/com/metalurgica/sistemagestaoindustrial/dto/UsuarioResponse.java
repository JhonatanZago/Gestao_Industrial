package com.metalurgica.sistemagestaoindustrial.dto;

public record UsuarioResponse(Long id, Long empresaId, String nome, String email, String status, String perfil) {}
