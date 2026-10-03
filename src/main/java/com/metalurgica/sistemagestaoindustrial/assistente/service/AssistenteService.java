package com.metalurgica.sistemagestaoindustrial.assistente.service;

import com.metalurgica.sistemagestaoindustrial.assistente.*;
import com.metalurgica.sistemagestaoindustrial.assistente.ai.ChatModelGateway;
import com.metalurgica.sistemagestaoindustrial.assistente.ai.AiProviderException;
import com.metalurgica.sistemagestaoindustrial.assistente.config.AiProperties;
import com.metalurgica.sistemagestaoindustrial.assistente.dto.*;
import com.metalurgica.sistemagestaoindustrial.assistente.grounding.GroundingValidator;
import com.metalurgica.sistemagestaoindustrial.assistente.grounding.PromptContextAssembler;
import com.metalurgica.sistemagestaoindustrial.assistente.grounding.ResponseSanitizer;
import com.metalurgica.sistemagestaoindustrial.assistente.planning.AssistenteQuestionPlanner;
import com.metalurgica.sistemagestaoindustrial.assistente.rag.RagDocumentService;
import com.metalurgica.sistemagestaoindustrial.assistente.security.AiRateLimiter;
import com.metalurgica.sistemagestaoindustrial.assistente.security.AssistenteSecurityContextResolver;
import com.metalurgica.sistemagestaoindustrial.assistente.telemetry.AiTelemetry;
import com.metalurgica.sistemagestaoindustrial.assistente.tool.*;
import com.metalurgica.sistemagestaoindustrial.resilience.RemoteCallPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class AssistenteService {
    private final AiProperties properties;
    private final ChatModelGateway chatGateway;
    private final AiToolCatalog toolCatalog;
    private final AssistenteSecurityContextResolver contextResolver;
    private final AssistenteQuestionPlanner planner;
    private final PromptContextAssembler promptContextAssembler;
    private final GroundingValidator groundingValidator;
    private final ResponseSanitizer responseSanitizer;
    private final RagDocumentService ragDocumentService;
    private final AiRateLimiter rateLimiter;
    private final AiTelemetry telemetry;
    private final Clock clock;
    private final RemoteCallPolicy remoteCallPolicy;

    public AssistenteService(AiProperties properties, ChatModelGateway chatGateway, AiToolCatalog toolCatalog,
                             AssistenteSecurityContextResolver contextResolver, AssistenteQuestionPlanner planner,
                             RagDocumentService ragDocumentService, AiRateLimiter rateLimiter, AiTelemetry telemetry,
                             RemoteCallPolicy remoteCallPolicy) {
        this.properties = properties;
        this.chatGateway = chatGateway;
        this.toolCatalog = toolCatalog;
        this.contextResolver = contextResolver;
        this.planner = planner;
        this.promptContextAssembler = new PromptContextAssembler();
        this.groundingValidator = new GroundingValidator();
        this.responseSanitizer = new ResponseSanitizer();
        this.ragDocumentService = ragDocumentService;
        this.rateLimiter = rateLimiter;
        this.telemetry = telemetry;
        this.remoteCallPolicy = remoteCallPolicy;
        this.clock = Clock.system(ZoneId.of(properties.getTimezone()));
    }

    public RespostaAssistente ask(Long empresaId, PerguntaAssistenteRequest request, Authentication authentication) {
        var started = System.nanoTime();
        var now = OffsetDateTime.now(clock);
        var context = contextResolver.resolve(authentication, empresaId);
        if (!properties.isEnabled()) {
            telemetry.request(context.empresaId(), context.userId(), "disabled", "none", elapsed(started), 0, 0, 0, "AI_DISABLED");
            return unavailable(AssistenteErrorCode.AI_DISABLED, "Assistente de IA desabilitado.", now);
        }
        try {
            if (rateLimiter != null) rateLimiter.acquire(context);
        } catch (AiToolException exception) {
            telemetry.request(context.empresaId(), context.userId(), "local", "none", elapsed(started), 0, 0, 0, exception.getCode().name());
            return safeError(exception.getCode(), exception.getMessage(), request == null ? null : request.conversationId(), now);
        }
        if (request == null || request.question() == null || request.question().isBlank()
                || request.question().length() > properties.getMaxQuestionLength()) {
            return unavailable(AssistenteErrorCode.AI_INVALID_REQUEST, "Pergunta inválida ou acima do limite.", now);
        }
        var plan = planner.plan(request.question(), clock);
        if (plan.status() == AssistenteStatus.PRECISA_CLARIFICACAO) {
            telemetry.request(context.empresaId(), context.userId(), "configured", "unknown", elapsed(started), 0, 0, 0, "PRECISA_CLARIFICACAO");
            return new RespostaAssistente(AssistenteStatus.PRECISA_CLARIFICACAO, null, plan.clarification(),
                    request.conversationId(), List.of(), null, null, List.of(), List.of(), now);
        }
        try {
            AiToolResult toolResult;
            List<String> documents = List.of();
            if ("rag_consulta".equals(plan.operation()) && ragDocumentService != null) {
                var matches = ragDocumentService.search(context, request.question(), properties.getMaxRetrievedDocuments());
                documents = matches.stream().map(com.metalurgica.sistemagestaoindustrial.assistente.ai.VectorStorePort.VectorMatch::text).toList();
                var sources = matches.stream().map(match -> new FonteAssistente("RAG", match.chunkId(), "Documento interno", null, null)).toList();
                toolResult = new AiToolResult("rag_consulta", java.util.Map.of("documents", documents), sources, null, null,
                        documents.isEmpty() ? List.of(new AvisoAssistente("AI_NO_DATA", "Nenhum documento autorizado foi encontrado.", "INFO")) : List.of());
            } else {
                toolResult = toolCatalog.execute(new AiToolRequest(plan.operation(), plan.arguments(), context, authentication, 1));
            }
            var answer = complete(request.question(), toolResult, documents);
            var grounding = groundingValidator.validate(responseSanitizer.sanitize(answer), toolResult);
            var warnings = new ArrayList<>(toolResult.warnings());
            if (!grounding.valid()) {
                warnings.add(new AvisoAssistente("AI_GROUNDING_FAILED",
                        "Alguns claims não puderam ser vinculados às fontes.", "ALERTA"));
            }
            var status = toolResult.warnings().stream().anyMatch(w -> "AI_NO_DATA".equals(w.codigo()))
                    ? AssistenteStatus.SEM_DADOS : AssistenteStatus.RESPONDIDO;
            var code = status == AssistenteStatus.SEM_DADOS ? AssistenteErrorCode.AI_NO_DATA : null;
            telemetry.request(context.empresaId(), context.userId(), "configured", "unknown", elapsed(started), 0, 0, 1, status.name());
            return new RespostaAssistente(status, code, grounding.answer(), request.conversationId(), toolResult.sources(),
                    toolResult.period(), toolResult.qualityData(), warnings, toolResult.sources(), now);
        } catch (AiToolException exception) {
            telemetry.request(context.empresaId(), context.userId(), "local", "unknown", elapsed(started), 0, 0, 1, exception.getCode().name());
            return safeError(exception.getCode(), exception.getMessage(), request.conversationId(), now);
        } catch (AiProviderException exception) {
            telemetry.request(context.empresaId(), context.userId(), "provider", "unknown", elapsed(started), 0, 0, 1, exception.getCode().name());
            return safeError(exception.getCode(), exception.getMessage(), request.conversationId(), now);
        }
    }

    private long elapsed(long started) {
        return (System.nanoTime() - started) / 1_000_000;
    }

    private String complete(String question, AiToolResult toolResult, List<String> documents) {
        var messages = promptContextAssembler.assemble(question, toolResult, documents);
        var definitions = toolCatalog.definitions().stream()
                .map(d -> new ChatModelGateway.ToolDefinition(d.name(), d.description(), d.inputSchema())).toList();
        var response = remoteCallPolicy.execute(RemoteCallPolicy.Operation.READ_IDEMPOTENT,
                () -> chatGateway.complete(new ChatModelGateway.ChatModelRequest(messages, definitions,
                        properties.getMaxOutputTokens())));
        return response.answer() == null ? "Não foi possível gerar uma resposta textual fundamentada." : response.answer();
    }

    private RespostaAssistente unavailable(AssistenteErrorCode code, String message, OffsetDateTime now) {
        return new RespostaAssistente(AssistenteStatus.INDISPONIVEL, code, message, null,
                List.of(), null, null, List.of(), List.of(), now);
    }

    private RespostaAssistente safeError(AssistenteErrorCode code, String message, String conversationId, OffsetDateTime now) {
        var status = code == AssistenteErrorCode.AI_PERMISSION_DENIED ? AssistenteStatus.SEM_PERMISSAO : AssistenteStatus.INDISPONIVEL;
        return new RespostaAssistente(status, code, message, conversationId, List.of(), null, null, List.of(), List.of(), now);
    }
}
