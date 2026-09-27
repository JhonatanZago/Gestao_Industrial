package com.metalurgica.sistemagestaoindustrial.dto.response;

import java.time.OffsetDateTime;

public record SetorResponse(Long id, Long empresaId, String codigo, String nome, String descricao,
                            boolean ativo, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
}
