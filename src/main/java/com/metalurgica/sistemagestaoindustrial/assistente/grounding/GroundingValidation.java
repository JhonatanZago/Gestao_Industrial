package com.metalurgica.sistemagestaoindustrial.assistente.grounding;

import java.util.List;

public record GroundingValidation(boolean valid, String answer, List<String> unsupportedClaims) {
    public GroundingValidation {
        unsupportedClaims = unsupportedClaims == null ? List.of() : List.copyOf(unsupportedClaims);
    }
}
