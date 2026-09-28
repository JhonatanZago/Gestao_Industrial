package com.metalurgica.sistemagestaoindustrial.dto;

import java.util.List;

public record MeResponse(Long id, String nome, String email, Long empresaId, String perfil,
                         List<String> permissoes, List<Long> setores) {}
