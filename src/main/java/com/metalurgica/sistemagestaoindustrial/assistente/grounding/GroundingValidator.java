package com.metalurgica.sistemagestaoindustrial.assistente.grounding;

import com.metalurgica.sistemagestaoindustrial.assistente.tool.AiToolResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class GroundingValidator {
    private static final Pattern NUMBER = Pattern.compile("(?<![\\p{L}\\d])\\d+(?:[.,]\\d+)?(?![\\p{L}\\d])");

    public GroundingValidation validate(String answer, AiToolResult result) {
        if (answer == null || answer.isBlank()) {
            return new GroundingValidation(false, "Não há resposta textual fundamentada disponível.", List.of("resposta vazia"));
        }
        var sourceText = String.valueOf(result == null ? "" : result.data());
        var unsupported = new ArrayList<String>();
        var matcher = NUMBER.matcher(answer);
        while (matcher.find()) {
            var token = matcher.group();
            if (!containsEquivalentNumber(sourceText, token)) unsupported.add(token);
        }
        if (unsupported.isEmpty()) return new GroundingValidation(true, answer, List.of());
        var sanitized = answer;
        for (var token : unsupported) sanitized = sanitized.replace(token, "[não fundamentado]");
        return new GroundingValidation(false, sanitized, unsupported);
    }

    private boolean containsEquivalentNumber(String source, String token) {
        var expected = parse(token);
        var matcher = NUMBER.matcher(source);
        while (matcher.find()) {
            if (expected.compareTo(parse(matcher.group())) == 0) return true;
        }
        return false;
    }

    private BigDecimal parse(String value) {
        var normalized = value.contains(",") && value.contains(".")
                ? value.replace(".", "").replace(',', '.')
                : value.replace(',', '.');
        return new BigDecimal(normalized);
    }
}
