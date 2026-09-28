package com.metalurgica.sistemagestaoindustrial.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.metalurgica.sistemagestaoindustrial.quality.StatusConfiabilidadeDados;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record QualidadeImportacaoResponse(
        Long importacaoId,
        int linhasImportadas,
        long linhasComAlerta,
        long duplicidades,
        long semTempoPadrao,
        long semValorIndustrial,
        StatusConfiabilidadeDados status) { }
