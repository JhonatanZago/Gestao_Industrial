package com.metalurgica.sistemagestaoindustrial.dto.response;

import java.time.OffsetDateTime;

public record PecaResponse(Long id, Long empresaId, String codigo, String descricao, boolean ativo,
                           OffsetDateTime createdAt, OffsetDateTime updatedAt) {
}
