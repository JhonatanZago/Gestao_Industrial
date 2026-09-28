package com.metalurgica.sistemagestaoindustrial.assistente.dto;

import com.metalurgica.sistemagestaoindustrial.assistente.AssistenteStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssistenteDtoTests {
    @Test
    void requestNaoCarregaCamposDeAutorizacao() {
        var request = new PerguntaAssistenteRequest("produção de ontem", null);
        assertEquals("produção de ontem", request.question());
        assertEquals(2, PerguntaAssistenteRequest.class.getRecordComponents().length);
    }

    @Test
    void periodoNaoAceitaFimAnterior() {
        assertThrows(IllegalArgumentException.class, () -> new PeriodoAssistente(
                LocalDateTime.of(2026, 1, 2, 0, 0), LocalDateTime.of(2026, 1, 1, 0, 0)));
    }

    @Test
    void respostaProtegeListasMutaveis() {
        var sources = new ArrayList<FonteAssistente>();
        var response = new RespostaAssistente(AssistenteStatus.RESPONDIDO, null, "ok", null,
                sources, null, null, null, null, null);
        sources.add(new FonteAssistente("tool", "1", "produção", null, null));
        assertEquals(0, response.sources().size());
        assertEquals(0, response.dataUsed().size());
    }
}
