package com.metalurgica.sistemagestaoindustrial.assistente.grounding;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ResponseSanitizer {
    private static final Pattern THINK = Pattern.compile("(?is)<think>.*?</think>");
    private static final Pattern SECRET = Pattern.compile("(?i)(api[-_ ]?key|authorization|bearer|secret|token)\\s*[:=]\\s*[^\\s,;]+") ;

    public String sanitize(String answer) {
        if (answer == null) return "";
        return SECRET.matcher(THINK.matcher(answer).replaceAll("")).replaceAll("[conteúdo removido]").trim();
    }
}
