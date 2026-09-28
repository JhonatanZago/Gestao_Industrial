package com.metalurgica.sistemagestaoindustrial.assistente.dto;

import com.metalurgica.sistemagestaoindustrial.assistente.AssistenteErrorCode;
import com.metalurgica.sistemagestaoindustrial.assistente.AssistenteStatus;

import java.time.OffsetDateTime;
import java.util.List;

public record RespostaAssistente(
        AssistenteStatus status,
        AssistenteErrorCode errorCode,
        String answer,
        String conversationId,
        List<FonteAssistente> dataUsed,
        PeriodoAssistente period,
        QualidadeDadosAssistente qualityData,
        List<AvisoAssistente> warnings,
        List<FonteAssistente> sources,
        OffsetDateTime generatedAt) {

    public RespostaAssistente {
        dataUsed = dataUsed == null ? List.of() : List.copyOf(dataUsed);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        sources = sources == null ? List.of() : List.copyOf(sources);
    }
}
