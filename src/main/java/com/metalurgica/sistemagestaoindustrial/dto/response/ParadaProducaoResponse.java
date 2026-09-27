package com.metalurgica.sistemagestaoindustrial.dto.response;
import java.time.*;
public record ParadaProducaoResponse(Long id, Long empresaId, Long producaoId, Long motivoParadaId, String motivoCodigo, String motivoNome, Long maquinaId, String maquinaCodigo, String maquinaNome, LocalDateTime inicio, LocalDateTime fim, long duracaoMinutos, String observacao, boolean ativo, OffsetDateTime createdAt, OffsetDateTime updatedAt) { }
