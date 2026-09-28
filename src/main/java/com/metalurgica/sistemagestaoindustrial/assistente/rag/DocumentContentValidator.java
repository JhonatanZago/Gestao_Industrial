package com.metalurgica.sistemagestaoindustrial.assistente.rag;

import com.metalurgica.sistemagestaoindustrial.assistente.AssistenteErrorCode;
import com.metalurgica.sistemagestaoindustrial.assistente.config.AiProperties;
import com.metalurgica.sistemagestaoindustrial.assistente.tool.AiToolException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class DocumentContentValidator {
    private static final Pattern SAFE_NAME = Pattern.compile("[A-Za-z0-9À-ÿ][A-Za-z0-9À-ÿ ._()\\-]{0,254}");
    private final AiProperties properties;

    public DocumentContentValidator(AiProperties properties) {
        this.properties = properties;
    }

    public String validateAndNormalize(String fileName, String contentType, byte[] bytes) {
        if (fileName == null || !SAFE_NAME.matcher(fileName).matches() || fileName.contains("..")) {
            throw new AiToolException(AssistenteErrorCode.AI_DOCUMENT_INVALID, "Nome de documento inválido.");
        }
        if (bytes == null || bytes.length == 0 || bytes.length > properties.getMaxDocumentBytes()) {
            throw new AiToolException(AssistenteErrorCode.AI_DOCUMENT_INVALID, "Tamanho de documento inválido.");
        }
        var lower = fileName.toLowerCase(Locale.ROOT);
        var text = lower.endsWith(".txt") || lower.endsWith(".md");
        var pdf = lower.endsWith(".pdf");
        if (!text && !(pdf && properties.isPdfEnabled())) {
            throw new AiToolException(AssistenteErrorCode.AI_DOCUMENT_INVALID, "Formato de documento não suportado.");
        }
        if (pdf) throw new AiToolException(AssistenteErrorCode.AI_DOCUMENT_INVALID, "Extractor PDF seguro não configurado.");
        var normalized = new String(bytes, StandardCharsets.UTF_8).replace("\u0000", "").replace("\r\n", "\n").trim();
        if (normalized.isBlank() || normalized.contains("<script") || normalized.contains("<?php")) {
            throw new AiToolException(AssistenteErrorCode.AI_DOCUMENT_INVALID, "Conteúdo documental não é extraível ou contém script.");
        }
        return normalized;
    }
}
