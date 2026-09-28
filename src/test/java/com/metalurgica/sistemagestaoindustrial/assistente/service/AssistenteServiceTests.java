package com.metalurgica.sistemagestaoindustrial.assistente.service;

import com.metalurgica.sistemagestaoindustrial.assistente.AssistenteErrorCode;
import com.metalurgica.sistemagestaoindustrial.assistente.AssistenteStatus;
import com.metalurgica.sistemagestaoindustrial.assistente.ai.ChatModelGateway;
import com.metalurgica.sistemagestaoindustrial.assistente.config.AiProperties;
import com.metalurgica.sistemagestaoindustrial.assistente.dto.PerguntaAssistenteRequest;
import com.metalurgica.sistemagestaoindustrial.assistente.planning.AssistenteQuestionPlanner;
import com.metalurgica.sistemagestaoindustrial.assistente.rag.RagDocumentService;
import com.metalurgica.sistemagestaoindustrial.assistente.security.AiRateLimiter;
import com.metalurgica.sistemagestaoindustrial.assistente.security.AssistenteSecurityContextResolver;
import com.metalurgica.sistemagestaoindustrial.assistente.tool.AiToolCatalog;
import com.metalurgica.sistemagestaoindustrial.assistente.telemetry.AiTelemetry;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AssistenteServiceTests {
    @Test
    void iaDesabilitadaRetornaEstadoControlado() {
        var properties = new AiProperties();
        var resolver = Mockito.mock(AssistenteSecurityContextResolver.class);
        Mockito.when(resolver.resolve(Mockito.any(), Mockito.eq(1L))).thenReturn(
                new com.metalurgica.sistemagestaoindustrial.assistente.security.AssistenteSecurityContext(
                        2L, 1L, "ADMINISTRADOR", java.util.Set.of("ANALYTICS_VISUALIZAR"), java.util.Set.of(), true));
        var service = new AssistenteService(properties, Mockito.mock(ChatModelGateway.class),
                Mockito.mock(AiToolCatalog.class), resolver, new AssistenteQuestionPlanner(properties),
                Mockito.mock(RagDocumentService.class), Mockito.mock(AiRateLimiter.class), Mockito.mock(AiTelemetry.class));
        var response = service.ask(1L, new PerguntaAssistenteRequest("produção de ontem", null), Mockito.mock(Authentication.class));
        assertEquals(AssistenteStatus.INDISPONIVEL, response.status());
        assertEquals(AssistenteErrorCode.AI_DISABLED, response.errorCode());
    }
}
