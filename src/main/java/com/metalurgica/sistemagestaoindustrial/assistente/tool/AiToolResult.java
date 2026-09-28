package com.metalurgica.sistemagestaoindustrial.assistente.tool;

import com.metalurgica.sistemagestaoindustrial.assistente.dto.AvisoAssistente;
import com.metalurgica.sistemagestaoindustrial.assistente.dto.FonteAssistente;
import com.metalurgica.sistemagestaoindustrial.assistente.dto.PeriodoAssistente;
import com.metalurgica.sistemagestaoindustrial.assistente.dto.QualidadeDadosAssistente;

import java.util.List;
import java.util.Map;

public record AiToolResult(
        String toolName,
        Map<String, Object> data,
        List<FonteAssistente> sources,
        PeriodoAssistente period,
        QualidadeDadosAssistente qualityData,
        List<AvisoAssistente> warnings) {
    public AiToolResult {
        data = data == null ? Map.of() : Map.copyOf(data);
        sources = sources == null ? List.of() : List.copyOf(sources);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
