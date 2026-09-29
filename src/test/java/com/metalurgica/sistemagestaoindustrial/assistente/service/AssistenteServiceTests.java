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
import com.metalurgica.sistemagestaoindustrial.resilience.RemoteCallPolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                Mockito.mock(RagDocumentService.class), Mockito.mock(AiRateLimiter.class), Mockito.mock(AiTelemetry.class),
                Mockito.mock(RemoteCallPolicy.class));
        var response = service.ask(1L, new PerguntaAssistenteRequest("produção de ontem", null), Mockito.mock(Authentication.class));
        assertEquals(AssistenteStatus.INDISPONIVEL, response.status());
        assertEquals(AssistenteErrorCode.AI_DISABLED, response.errorCode());
    }

    @Test
    void falhaTransitoriaEsgotadaRetornaIndisponibilidadeSemInventarResposta() {
        var properties = new AiProperties(); properties.setEnabled(true);
        var resolver = Mockito.mock(AssistenteSecurityContextResolver.class);
        Mockito.when(resolver.resolve(Mockito.any(), Mockito.eq(1L))).thenReturn(
                new com.metalurgica.sistemagestaoindustrial.assistente.security.AssistenteSecurityContext(
                        2L, 1L, "ADMINISTRADOR", java.util.Set.of("ANALYTICS_VISUALIZAR"), java.util.Set.of(), true));
        var tools = Mockito.mock(AiToolCatalog.class);
        Mockito.when(tools.definitions()).thenReturn(java.util.List.of());
        Mockito.when(tools.execute(Mockito.any())).thenReturn(new com.metalurgica.sistemagestaoindustrial.assistente.tool.AiToolResult(
                "producao_resumo", java.util.Map.of("quantidade", 10), java.util.List.of(), null, null, java.util.List.of()));
        var gateway = Mockito.mock(ChatModelGateway.class);
        Mockito.when(gateway.complete(Mockito.any())).thenThrow(new com.metalurgica.sistemagestaoindustrial.assistente.ai.AiProviderException(
                AssistenteErrorCode.AI_TIMEOUT, "Provedor indisponível.", true));
        var operational = new com.metalurgica.sistemagestaoindustrial.observability.OperationalProperties();
        operational.setRemoteMaxAttempts(2); operational.setRemoteBackoff(java.time.Duration.ZERO);
        var service = new AssistenteService(properties, gateway, tools, resolver,
                new AssistenteQuestionPlanner(properties), Mockito.mock(RagDocumentService.class),
                Mockito.mock(AiRateLimiter.class), Mockito.mock(AiTelemetry.class), new RemoteCallPolicy(operational));

        var response = service.ask(1L, new PerguntaAssistenteRequest("producao de ontem", "conversation-1"),
                Mockito.mock(Authentication.class));

        assertEquals(AssistenteStatus.INDISPONIVEL, response.status());
        assertEquals(AssistenteErrorCode.AI_TIMEOUT, response.errorCode());
        assertTrue(response.answer().contains("indisponível"));
        Mockito.verify(gateway, Mockito.times(2)).complete(Mockito.any());
    }
}
