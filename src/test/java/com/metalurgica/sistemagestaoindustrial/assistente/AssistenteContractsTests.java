package com.metalurgica.sistemagestaoindustrial.assistente;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class AssistenteContractsTests {
    @Test
    void preservaStatusPublicosDoContrato() {
        assertArrayEquals(new String[]{
                "RESPONDIDO",
                "SEM_DADOS",
                "SEM_PERMISSAO",
                "PRECISA_CLARIFICACAO",
                "INDISPONIVEL"
        }, java.util.Arrays.stream(AssistenteStatus.values()).map(Enum::name).toArray(String[]::new));
    }

    @Test
    void preservaCodigosOperacionaisDaIA() {
        var names = java.util.Arrays.stream(AssistenteErrorCode.values()).map(Enum::name).toList();
        assertArrayEquals(new String[]{
                "AI_DISABLED",
                "AI_PROVIDER_UNAVAILABLE",
                "AI_TIMEOUT",
                "AI_RATE_LIMIT",
                "AI_PERMISSION_DENIED",
                "AI_NO_DATA",
                "AI_INVALID_REQUEST",
                "AI_GROUNDING_FAILED",
                "AI_TOOL_REJECTED",
                "AI_DOCUMENT_INVALID"
        }, names.toArray(String[]::new));
    }
}
