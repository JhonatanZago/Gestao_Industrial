package com.metalurgica.sistemagestaoindustrial.assistente.dto;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class AssistenteRequestValidationTests {
    @Test
    void rejeitaConversationIdComFormatoDeControle() {
        var validator = Validation.buildDefaultValidatorFactory().getValidator();
        var violations = validator.validate(new PerguntaAssistenteRequest("produção ontem", "id com espaço"));
        assertFalse(violations.isEmpty());
    }
}
