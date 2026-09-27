package com.metalurgica.sistemagestaoindustrial.dto.response;
import java.time.OffsetDateTime;
public record ImportacaoProducaoResponse(Long id,Long empresaId,Long setorId,String setorCodigo,String setorNome,String nomeArquivo,String hashArquivo,String status,int totalLinhas,int linhasValidas,int linhasInvalidas,int linhasImportadas,OffsetDateTime iniciadoEm,OffsetDateTime finalizadoEm,OffsetDateTime createdAt) { }
