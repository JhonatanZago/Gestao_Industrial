package com.metalurgica.sistemagestaoindustrial.dto.response;

import java.time.OffsetDateTime;

public record FuncionarioResponse(Long id, Long empresaId, String codigo, String nome, boolean ativo,
                                  OffsetDateTime createdAt, OffsetDateTime updatedAt) {
}
