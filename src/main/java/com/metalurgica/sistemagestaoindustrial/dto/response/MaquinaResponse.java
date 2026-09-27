package com.metalurgica.sistemagestaoindustrial.dto.response;
import java.time.OffsetDateTime;
public record MaquinaResponse(Long id, Long empresaId, Long setorId, String setorCodigo, String setorNome, String codigo, String nome, String descricao, String tipo, boolean ativo, OffsetDateTime createdAt, OffsetDateTime updatedAt) { }
