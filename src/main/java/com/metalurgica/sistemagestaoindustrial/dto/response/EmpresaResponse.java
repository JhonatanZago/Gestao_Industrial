package com.metalurgica.sistemagestaoindustrial.dto.response;

import java.time.OffsetDateTime;

public record EmpresaResponse(Long id, String razaoSocial, String nomeFantasia, String cnpj,
                              boolean ativo, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
}
