package com.metalurgica.sistemagestaoindustrial.dto.response;
import java.time.OffsetDateTime;
public record MotivoParadaResponse(Long id, Long empresaId, String codigo, String nome, String descricao, String categoria, boolean ativo, OffsetDateTime createdAt, OffsetDateTime updatedAt) { }
