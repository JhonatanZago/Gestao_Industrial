package com.metalurgica.sistemagestaoindustrial.assistente.planning;

import com.metalurgica.sistemagestaoindustrial.assistente.config.AiProperties;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AssistenteQuestionPlannerTests {
    private final AssistenteQuestionPlanner planner = new AssistenteQuestionPlanner(new AiProperties());
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-28T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    @Test
    void resolveOntemNoBackend() {
        var plan = planner.plan("qual foi a produção de ontem?", clock);
        assertEquals("producao_resumo", plan.operation());
        assertEquals("2026-09-27T00:00", plan.arguments().get("inicio"));
    }

    @Test
    void pedeClarificacaoQuandoMetricaOuPeriodoFaltam() {
        assertEquals("PRECISA_CLARIFICACAO", planner.plan("me mostre os dados", clock).status().name());
        assertEquals("PRECISA_CLARIFICACAO", planner.plan("qual foi a produção?", clock).status().name());
    }
}
